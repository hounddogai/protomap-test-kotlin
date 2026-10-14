package com.acme.transport.network.cutoff.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff policies by code. */
interface CutoffPolicyStore {
    fun find(code: String): CutoffPolicy?
    fun save(item: CutoffPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffPolicyStore(
    var items: MutableMap<String, CutoffPolicy>,
    var order: MutableList<String>,
) : CutoffPolicyStore {
    override fun find(code: String): CutoffPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffPolicy) {
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

    override fun list(): MutableList<CutoffPolicy> {
        val out = mutableListOf<CutoffPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffPolicyStore(): CutoffPolicyStore {
    return MemoryCutoffPolicyStore(mutableMapOf<String, CutoffPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffPolicyStore(
    var inner: CutoffPolicyStore,
    var cache: TtlCache<CutoffPolicy>,
    var hits: Int,
    var misses: Int,
) : CutoffPolicyStore {
    override fun find(code: String): CutoffPolicy? {
        val hit = (cache.get(code) as CutoffPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffPolicy> = inner.list()
}

fun newCachedCutoffPolicyStore(inner: CutoffPolicyStore, ttl: Int, clock: Clock): CutoffPolicyStore {
    return CachedCutoffPolicyStore(
        inner = inner,
        cache = newTtlCache<CutoffPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff policies. Empty fields match all. */
data class CutoffPolicyQuery(
    var locale: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffPolicy): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffPolicy>): MutableList<CutoffPolicy> {
        val matched = filter(items) { candidate: CutoffPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffPolicies(store: CutoffPolicyStore, query: CutoffPolicyQuery): MutableList<CutoffPolicy> {
    return query.apply(store.list())
}
