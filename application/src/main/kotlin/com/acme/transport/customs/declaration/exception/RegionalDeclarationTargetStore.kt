package com.acme.transport.customs.declaration.exception

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional declaration targets by code. */
interface RegionalDeclarationTargetStore {
    fun find(code: String): RegionalDeclarationTarget?
    fun save(item: RegionalDeclarationTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDeclarationTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDeclarationTargetStore(
    var items: MutableMap<String, RegionalDeclarationTarget>,
    var order: MutableList<String>,
) : RegionalDeclarationTargetStore {
    override fun find(code: String): RegionalDeclarationTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDeclarationTarget) {
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

    override fun list(): MutableList<RegionalDeclarationTarget> {
        val out = mutableListOf<RegionalDeclarationTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDeclarationTargetStore(): RegionalDeclarationTargetStore {
    return MemoryRegionalDeclarationTargetStore(
        mutableMapOf<String, RegionalDeclarationTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDeclarationTargetStore(
    var inner: RegionalDeclarationTargetStore,
    var cache: TtlCache<RegionalDeclarationTarget>,
    var hits: Int,
    var misses: Int,
) : RegionalDeclarationTargetStore {
    override fun find(code: String): RegionalDeclarationTarget? {
        val hit = (cache.get(code) as RegionalDeclarationTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDeclarationTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDeclarationTarget> = inner.list()
}

fun newCachedRegionalDeclarationTargetStore(inner: RegionalDeclarationTargetStore, ttl: Int, clock: Clock): RegionalDeclarationTargetStore {
    return CachedRegionalDeclarationTargetStore(
        inner = inner,
        cache = newTtlCache<RegionalDeclarationTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional declaration targets. Empty fields match all. */
data class RegionalDeclarationTargetQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDeclarationTarget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalDeclarationTarget>): MutableList<RegionalDeclarationTarget> {
        val matched = filter(items) { candidate: RegionalDeclarationTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDeclarationTargets(store: RegionalDeclarationTargetStore, query: RegionalDeclarationTargetQuery): MutableList<RegionalDeclarationTarget> {
    return query.apply(store.list())
}
