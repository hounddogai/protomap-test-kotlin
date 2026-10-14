package com.acme.transport.lastmile.parcel.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps fallback parcel targets by code. */
interface FallbackParcelTargetStore {
    fun find(code: String): FallbackParcelTarget?
    fun save(item: FallbackParcelTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackParcelTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackParcelTargetStore(
    var items: MutableMap<String, FallbackParcelTarget>,
    var order: MutableList<String>,
) : FallbackParcelTargetStore {
    override fun find(code: String): FallbackParcelTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackParcelTarget) {
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

    override fun list(): MutableList<FallbackParcelTarget> {
        val out = mutableListOf<FallbackParcelTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackParcelTargetStore(): FallbackParcelTargetStore {
    return MemoryFallbackParcelTargetStore(
        mutableMapOf<String, FallbackParcelTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackParcelTargetStore(
    var inner: FallbackParcelTargetStore,
    var cache: TtlCache<FallbackParcelTarget>,
    var hits: Int,
    var misses: Int,
) : FallbackParcelTargetStore {
    override fun find(code: String): FallbackParcelTarget? {
        val hit = (cache.get(code) as FallbackParcelTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackParcelTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackParcelTarget> = inner.list()
}

fun newCachedFallbackParcelTargetStore(inner: FallbackParcelTargetStore, ttl: Int, clock: Clock): FallbackParcelTargetStore {
    return CachedFallbackParcelTargetStore(
        inner = inner,
        cache = newTtlCache<FallbackParcelTarget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback parcel targets. Empty fields match all. */
data class FallbackParcelTargetQuery(
    var title: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackParcelTarget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackParcelTarget>): MutableList<FallbackParcelTarget> {
        val matched = filter(items) { candidate: FallbackParcelTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackParcelTargets(store: FallbackParcelTargetStore, query: FallbackParcelTargetQuery): MutableList<FallbackParcelTarget> {
    return query.apply(store.list())
}
