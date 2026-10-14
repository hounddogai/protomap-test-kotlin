package com.acme.transport.customs.declaration.segment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy declaration profiles by code. */
interface LegacyDeclarationProfileStore {
    fun find(code: String): LegacyDeclarationProfile?
    fun save(item: LegacyDeclarationProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDeclarationProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDeclarationProfileStore(
    var items: MutableMap<String, LegacyDeclarationProfile>,
    var order: MutableList<String>,
) : LegacyDeclarationProfileStore {
    override fun find(code: String): LegacyDeclarationProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDeclarationProfile) {
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

    override fun list(): MutableList<LegacyDeclarationProfile> {
        val out = mutableListOf<LegacyDeclarationProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDeclarationProfileStore(): LegacyDeclarationProfileStore {
    return MemoryLegacyDeclarationProfileStore(
        mutableMapOf<String, LegacyDeclarationProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDeclarationProfileStore(
    var inner: LegacyDeclarationProfileStore,
    var cache: TtlCache<LegacyDeclarationProfile>,
    var hits: Int,
    var misses: Int,
) : LegacyDeclarationProfileStore {
    override fun find(code: String): LegacyDeclarationProfile? {
        val hit = (cache.get(code) as LegacyDeclarationProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDeclarationProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDeclarationProfile> = inner.list()
}

fun newCachedLegacyDeclarationProfileStore(inner: LegacyDeclarationProfileStore, ttl: Int, clock: Clock): LegacyDeclarationProfileStore {
    return CachedLegacyDeclarationProfileStore(
        inner = inner,
        cache = newTtlCache<LegacyDeclarationProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy declaration profiles. Empty fields match all. */
data class LegacyDeclarationProfileQuery(
    var label: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDeclarationProfile): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyDeclarationProfile>): MutableList<LegacyDeclarationProfile> {
        val matched = filter(items) { candidate: LegacyDeclarationProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDeclarationProfiles(store: LegacyDeclarationProfileStore, query: LegacyDeclarationProfileQuery): MutableList<LegacyDeclarationProfile> {
    return query.apply(store.list())
}
