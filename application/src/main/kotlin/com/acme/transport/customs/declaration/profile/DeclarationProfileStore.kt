package com.acme.transport.customs.declaration.profile

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration profiles by code. */
interface DeclarationProfileStore {
    fun find(code: String): DeclarationProfile?
    fun save(item: DeclarationProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationProfileStore(
    var items: MutableMap<String, DeclarationProfile>,
    var order: MutableList<String>,
) : DeclarationProfileStore {
    override fun find(code: String): DeclarationProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationProfile) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<DeclarationProfile> {
        val out = mutableListOf<DeclarationProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationProfileStore(): DeclarationProfileStore {
    return MemoryDeclarationProfileStore(mutableMapOf<String, DeclarationProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationProfileStore(
    var inner: DeclarationProfileStore,
    var cache: TtlCache<DeclarationProfile>,
    var hits: Int,
    var misses: Int,
) : DeclarationProfileStore {
    override fun find(code: String): DeclarationProfile? {
        val hit = (cache.get(code) as DeclarationProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationProfile> = inner.list()
}

fun newCachedDeclarationProfileStore(inner: DeclarationProfileStore, ttl: Int, clock: Clock): DeclarationProfileStore {
    return CachedDeclarationProfileStore(
        inner = inner,
        cache = newTtlCache<DeclarationProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration profiles. Empty fields match all. */
data class DeclarationProfileQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationProfile): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationProfile>): MutableList<DeclarationProfile> {
        val matched = filter(items) { candidate: DeclarationProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationProfiles(store: DeclarationProfileStore, query: DeclarationProfileQuery): MutableList<DeclarationProfile> {
    return query.apply(store.list())
}
