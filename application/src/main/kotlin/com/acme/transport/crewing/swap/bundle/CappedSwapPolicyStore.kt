package com.acme.transport.crewing.swap.bundle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped swap policies by code. */
interface CappedSwapPolicyStore {
    fun find(code: String): CappedSwapPolicy?
    fun save(item: CappedSwapPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedSwapPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedSwapPolicyStore(
    var items: MutableMap<String, CappedSwapPolicy>,
    var order: MutableList<String>,
) : CappedSwapPolicyStore {
    override fun find(code: String): CappedSwapPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedSwapPolicy) {
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

    override fun list(): MutableList<CappedSwapPolicy> {
        val out = mutableListOf<CappedSwapPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedSwapPolicyStore(): CappedSwapPolicyStore {
    return MemoryCappedSwapPolicyStore(mutableMapOf<String, CappedSwapPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedSwapPolicyStore(
    var inner: CappedSwapPolicyStore,
    var cache: TtlCache<CappedSwapPolicy>,
    var hits: Int,
    var misses: Int,
) : CappedSwapPolicyStore {
    override fun find(code: String): CappedSwapPolicy? {
        val hit = (cache.get(code) as CappedSwapPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedSwapPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedSwapPolicy> = inner.list()
}

fun newCachedCappedSwapPolicyStore(inner: CappedSwapPolicyStore, ttl: Int, clock: Clock): CappedSwapPolicyStore {
    return CachedCappedSwapPolicyStore(
        inner = inner,
        cache = newTtlCache<CappedSwapPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped swap policies. Empty fields match all. */
data class CappedSwapPolicyQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedSwapPolicy): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedSwapPolicy>): MutableList<CappedSwapPolicy> {
        val matched = filter(items) { candidate: CappedSwapPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedSwapPolicies(store: CappedSwapPolicyStore, query: CappedSwapPolicyQuery): MutableList<CappedSwapPolicy> {
    return query.apply(store.list())
}
