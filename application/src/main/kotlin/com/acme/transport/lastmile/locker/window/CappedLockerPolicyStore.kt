package com.acme.transport.lastmile.locker.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps capped locker policies by code. */
interface CappedLockerPolicyStore {
    fun find(code: String): CappedLockerPolicy?
    fun save(item: CappedLockerPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedLockerPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedLockerPolicyStore(
    var items: MutableMap<String, CappedLockerPolicy>,
    var order: MutableList<String>,
) : CappedLockerPolicyStore {
    override fun find(code: String): CappedLockerPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedLockerPolicy) {
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

    override fun list(): MutableList<CappedLockerPolicy> {
        val out = mutableListOf<CappedLockerPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedLockerPolicyStore(): CappedLockerPolicyStore {
    return MemoryCappedLockerPolicyStore(mutableMapOf<String, CappedLockerPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedLockerPolicyStore(
    var inner: CappedLockerPolicyStore,
    var cache: TtlCache<CappedLockerPolicy>,
    var hits: Int,
    var misses: Int,
) : CappedLockerPolicyStore {
    override fun find(code: String): CappedLockerPolicy? {
        val hit = (cache.get(code) as CappedLockerPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedLockerPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedLockerPolicy> = inner.list()
}

fun newCachedCappedLockerPolicyStore(inner: CappedLockerPolicyStore, ttl: Int, clock: Clock): CappedLockerPolicyStore {
    return CachedCappedLockerPolicyStore(
        inner = inner,
        cache = newTtlCache<CappedLockerPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped locker policies. Empty fields match all. */
data class CappedLockerPolicyQuery(
    var source: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedLockerPolicy): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<CappedLockerPolicy>): MutableList<CappedLockerPolicy> {
        val matched = filter(items) { candidate: CappedLockerPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedLockerPolicies(store: CappedLockerPolicyStore, query: CappedLockerPolicyQuery): MutableList<CappedLockerPolicy> {
    return query.apply(store.list())
}
