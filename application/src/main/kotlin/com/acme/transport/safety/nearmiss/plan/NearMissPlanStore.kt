package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss plans by code. */
interface NearMissPlanStore {
    fun find(code: String): NearMissPlan?
    fun save(item: NearMissPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissPlanStore(
    var items: MutableMap<String, NearMissPlan>,
    var order: MutableList<String>,
) : NearMissPlanStore {
    override fun find(code: String): NearMissPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissPlan) {
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

    override fun list(): MutableList<NearMissPlan> {
        val out = mutableListOf<NearMissPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissPlanStore(): NearMissPlanStore {
    return MemoryNearMissPlanStore(mutableMapOf<String, NearMissPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissPlanStore(
    var inner: NearMissPlanStore,
    var cache: TtlCache<NearMissPlan>,
    var hits: Int,
    var misses: Int,
) : NearMissPlanStore {
    override fun find(code: String): NearMissPlan? {
        val hit = (cache.get(code) as NearMissPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissPlan> = inner.list()
}

fun newCachedNearMissPlanStore(inner: NearMissPlanStore, ttl: Int, clock: Clock): NearMissPlanStore {
    return CachedNearMissPlanStore(
        inner = inner,
        cache = newTtlCache<NearMissPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss plans. Empty fields match all. */
data class NearMissPlanQuery(
    var channel: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissPlan): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<NearMissPlan>): MutableList<NearMissPlan> {
        val matched = filter(items) { candidate: NearMissPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissPlans(store: NearMissPlanStore, query: NearMissPlanQuery): MutableList<NearMissPlan> {
    return query.apply(store.list())
}
