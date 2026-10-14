package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient dwell policies by code. */
interface LenientDwellPolicyStore {
    fun find(code: String): LenientDwellPolicy?
    fun save(item: LenientDwellPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDwellPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDwellPolicyStore(
    var items: MutableMap<String, LenientDwellPolicy>,
    var order: MutableList<String>,
) : LenientDwellPolicyStore {
    override fun find(code: String): LenientDwellPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDwellPolicy) {
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

    override fun list(): MutableList<LenientDwellPolicy> {
        val out = mutableListOf<LenientDwellPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDwellPolicyStore(): LenientDwellPolicyStore {
    return MemoryLenientDwellPolicyStore(mutableMapOf<String, LenientDwellPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDwellPolicyStore(
    var inner: LenientDwellPolicyStore,
    var cache: TtlCache<LenientDwellPolicy>,
    var hits: Int,
    var misses: Int,
) : LenientDwellPolicyStore {
    override fun find(code: String): LenientDwellPolicy? {
        val hit = (cache.get(code) as LenientDwellPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDwellPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDwellPolicy> = inner.list()
}

fun newCachedLenientDwellPolicyStore(inner: LenientDwellPolicyStore, ttl: Int, clock: Clock): LenientDwellPolicyStore {
    return CachedLenientDwellPolicyStore(
        inner = inner,
        cache = newTtlCache<LenientDwellPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient dwell policies. Empty fields match all. */
data class LenientDwellPolicyQuery(
    var segment: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDwellPolicy): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientDwellPolicy>): MutableList<LenientDwellPolicy> {
        val matched = filter(items) { candidate: LenientDwellPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDwellPolicies(store: LenientDwellPolicyStore, query: LenientDwellPolicyQuery): MutableList<LenientDwellPolicy> {
    return query.apply(store.list())
}
