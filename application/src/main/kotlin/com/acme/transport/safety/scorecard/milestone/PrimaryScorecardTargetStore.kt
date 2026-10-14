package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary scorecard targets by code. */
interface PrimaryScorecardTargetStore {
    fun find(code: String): PrimaryScorecardTarget?
    fun save(item: PrimaryScorecardTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryScorecardTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryScorecardTargetStore(
    var items: MutableMap<String, PrimaryScorecardTarget>,
    var order: MutableList<String>,
) : PrimaryScorecardTargetStore {
    override fun find(code: String): PrimaryScorecardTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryScorecardTarget) {
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

    override fun list(): MutableList<PrimaryScorecardTarget> {
        val out = mutableListOf<PrimaryScorecardTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryScorecardTargetStore(): PrimaryScorecardTargetStore {
    return MemoryPrimaryScorecardTargetStore(
        mutableMapOf<String, PrimaryScorecardTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryScorecardTargetStore(
    var inner: PrimaryScorecardTargetStore,
    var cache: TtlCache<PrimaryScorecardTarget>,
    var hits: Int,
    var misses: Int,
) : PrimaryScorecardTargetStore {
    override fun find(code: String): PrimaryScorecardTarget? {
        val hit = (cache.get(code) as PrimaryScorecardTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryScorecardTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryScorecardTarget> = inner.list()
}

fun newCachedPrimaryScorecardTargetStore(inner: PrimaryScorecardTargetStore, ttl: Int, clock: Clock): PrimaryScorecardTargetStore {
    return CachedPrimaryScorecardTargetStore(
        inner = inner,
        cache = newTtlCache<PrimaryScorecardTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary scorecard targets. Empty fields match all. */
data class PrimaryScorecardTargetQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryScorecardTarget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryScorecardTarget>): MutableList<PrimaryScorecardTarget> {
        val matched = filter(items) { candidate: PrimaryScorecardTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryScorecardTargets(store: PrimaryScorecardTargetStore, query: PrimaryScorecardTargetQuery): MutableList<PrimaryScorecardTarget> {
    return query.apply(store.list())
}
