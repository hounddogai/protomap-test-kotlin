package com.acme.transport.terminal.dwell.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell policies by code. */
interface DwellPolicyStore {
    fun find(code: String): DwellPolicy?
    fun save(item: DwellPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellPolicyStore(
    var items: MutableMap<String, DwellPolicy>,
    var order: MutableList<String>,
) : DwellPolicyStore {
    override fun find(code: String): DwellPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellPolicy) {
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

    override fun list(): MutableList<DwellPolicy> {
        val out = mutableListOf<DwellPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellPolicyStore(): DwellPolicyStore {
    return MemoryDwellPolicyStore(mutableMapOf<String, DwellPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellPolicyStore(
    var inner: DwellPolicyStore,
    var cache: TtlCache<DwellPolicy>,
    var hits: Int,
    var misses: Int,
) : DwellPolicyStore {
    override fun find(code: String): DwellPolicy? {
        val hit = (cache.get(code) as DwellPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellPolicy> = inner.list()
}

fun newCachedDwellPolicyStore(inner: DwellPolicyStore, ttl: Int, clock: Clock): DwellPolicyStore {
    return CachedDwellPolicyStore(
        inner = inner,
        cache = newTtlCache<DwellPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell policies. Empty fields match all. */
data class DwellPolicyQuery(
    var title: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellPolicy): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellPolicy>): MutableList<DwellPolicy> {
        val matched = filter(items) { candidate: DwellPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellPolicies(store: DwellPolicyStore, query: DwellPolicyQuery): MutableList<DwellPolicy> {
    return query.apply(store.list())
}
