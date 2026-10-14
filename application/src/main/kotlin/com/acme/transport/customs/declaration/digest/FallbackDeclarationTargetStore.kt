package com.acme.transport.customs.declaration.digest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback declaration targets by code. */
interface FallbackDeclarationTargetStore {
    fun find(code: String): FallbackDeclarationTarget?
    fun save(item: FallbackDeclarationTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDeclarationTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDeclarationTargetStore(
    var items: MutableMap<String, FallbackDeclarationTarget>,
    var order: MutableList<String>,
) : FallbackDeclarationTargetStore {
    override fun find(code: String): FallbackDeclarationTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDeclarationTarget) {
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

    override fun list(): MutableList<FallbackDeclarationTarget> {
        val out = mutableListOf<FallbackDeclarationTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDeclarationTargetStore(): FallbackDeclarationTargetStore {
    return MemoryFallbackDeclarationTargetStore(
        mutableMapOf<String, FallbackDeclarationTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDeclarationTargetStore(
    var inner: FallbackDeclarationTargetStore,
    var cache: TtlCache<FallbackDeclarationTarget>,
    var hits: Int,
    var misses: Int,
) : FallbackDeclarationTargetStore {
    override fun find(code: String): FallbackDeclarationTarget? {
        val hit = (cache.get(code) as FallbackDeclarationTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDeclarationTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDeclarationTarget> = inner.list()
}

fun newCachedFallbackDeclarationTargetStore(inner: FallbackDeclarationTargetStore, ttl: Int, clock: Clock): FallbackDeclarationTargetStore {
    return CachedFallbackDeclarationTargetStore(
        inner = inner,
        cache = newTtlCache<FallbackDeclarationTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback declaration targets. Empty fields match all. */
data class FallbackDeclarationTargetQuery(
    var region: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDeclarationTarget): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDeclarationTarget>): MutableList<FallbackDeclarationTarget> {
        val matched = filter(items) { candidate: FallbackDeclarationTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDeclarationTargets(store: FallbackDeclarationTargetStore, query: FallbackDeclarationTargetQuery): MutableList<FallbackDeclarationTarget> {
    return query.apply(store.list())
}
