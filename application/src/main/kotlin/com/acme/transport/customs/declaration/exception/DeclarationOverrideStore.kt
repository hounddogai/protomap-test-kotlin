package com.acme.transport.customs.declaration.exception

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration overrides by code. */
interface DeclarationOverrideStore {
    fun find(code: String): DeclarationOverride?
    fun save(item: DeclarationOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationOverrideStore(
    var items: MutableMap<String, DeclarationOverride>,
    var order: MutableList<String>,
) : DeclarationOverrideStore {
    override fun find(code: String): DeclarationOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationOverride) {
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

    override fun list(): MutableList<DeclarationOverride> {
        val out = mutableListOf<DeclarationOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationOverrideStore(): DeclarationOverrideStore {
    return MemoryDeclarationOverrideStore(mutableMapOf<String, DeclarationOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationOverrideStore(
    var inner: DeclarationOverrideStore,
    var cache: TtlCache<DeclarationOverride>,
    var hits: Int,
    var misses: Int,
) : DeclarationOverrideStore {
    override fun find(code: String): DeclarationOverride? {
        val hit = (cache.get(code) as DeclarationOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationOverride> = inner.list()
}

fun newCachedDeclarationOverrideStore(inner: DeclarationOverrideStore, ttl: Int, clock: Clock): DeclarationOverrideStore {
    return CachedDeclarationOverrideStore(
        inner = inner,
        cache = newTtlCache<DeclarationOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration overrides. Empty fields match all. */
data class DeclarationOverrideQuery(
    var name: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationOverride>): MutableList<DeclarationOverride> {
        val matched = filter(items) { candidate: DeclarationOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationOverrides(store: DeclarationOverrideStore, query: DeclarationOverrideQuery): MutableList<DeclarationOverride> {
    return query.apply(store.list())
}
