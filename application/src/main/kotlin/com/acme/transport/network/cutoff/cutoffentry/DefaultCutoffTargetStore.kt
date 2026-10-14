package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default cutoff targets by code. */
interface DefaultCutoffTargetStore {
    fun find(code: String): DefaultCutoffTarget?
    fun save(item: DefaultCutoffTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCutoffTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCutoffTargetStore(
    var items: MutableMap<String, DefaultCutoffTarget>,
    var order: MutableList<String>,
) : DefaultCutoffTargetStore {
    override fun find(code: String): DefaultCutoffTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCutoffTarget) {
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

    override fun list(): MutableList<DefaultCutoffTarget> {
        val out = mutableListOf<DefaultCutoffTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCutoffTargetStore(): DefaultCutoffTargetStore {
    return MemoryDefaultCutoffTargetStore(mutableMapOf<String, DefaultCutoffTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCutoffTargetStore(
    var inner: DefaultCutoffTargetStore,
    var cache: TtlCache<DefaultCutoffTarget>,
    var hits: Int,
    var misses: Int,
) : DefaultCutoffTargetStore {
    override fun find(code: String): DefaultCutoffTarget? {
        val hit = (cache.get(code) as DefaultCutoffTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCutoffTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCutoffTarget> = inner.list()
}

fun newCachedDefaultCutoffTargetStore(inner: DefaultCutoffTargetStore, ttl: Int, clock: Clock): DefaultCutoffTargetStore {
    return CachedDefaultCutoffTargetStore(
        inner = inner,
        cache = newTtlCache<DefaultCutoffTarget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default cutoff targets. Empty fields match all. */
data class DefaultCutoffTargetQuery(
    var depot: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCutoffTarget): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultCutoffTarget>): MutableList<DefaultCutoffTarget> {
        val matched = filter(items) { candidate: DefaultCutoffTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCutoffTargets(store: DefaultCutoffTargetStore, query: DefaultCutoffTargetQuery): MutableList<DefaultCutoffTarget> {
    return query.apply(store.list())
}
