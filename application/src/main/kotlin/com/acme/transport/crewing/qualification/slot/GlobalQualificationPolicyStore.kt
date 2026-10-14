package com.acme.transport.crewing.qualification.slot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global qualification policies by code. */
interface GlobalQualificationPolicyStore {
    fun find(code: String): GlobalQualificationPolicy?
    fun save(item: GlobalQualificationPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalQualificationPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalQualificationPolicyStore(
    var items: MutableMap<String, GlobalQualificationPolicy>,
    var order: MutableList<String>,
) : GlobalQualificationPolicyStore {
    override fun find(code: String): GlobalQualificationPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalQualificationPolicy) {
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

    override fun list(): MutableList<GlobalQualificationPolicy> {
        val out = mutableListOf<GlobalQualificationPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalQualificationPolicyStore(): GlobalQualificationPolicyStore {
    return MemoryGlobalQualificationPolicyStore(
        mutableMapOf<String, GlobalQualificationPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalQualificationPolicyStore(
    var inner: GlobalQualificationPolicyStore,
    var cache: TtlCache<GlobalQualificationPolicy>,
    var hits: Int,
    var misses: Int,
) : GlobalQualificationPolicyStore {
    override fun find(code: String): GlobalQualificationPolicy? {
        val hit = (cache.get(code) as GlobalQualificationPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalQualificationPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalQualificationPolicy> = inner.list()
}

fun newCachedGlobalQualificationPolicyStore(inner: GlobalQualificationPolicyStore, ttl: Int, clock: Clock): GlobalQualificationPolicyStore {
    return CachedGlobalQualificationPolicyStore(
        inner = inner,
        cache = newTtlCache<GlobalQualificationPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global qualification policies. Empty fields match all. */
data class GlobalQualificationPolicyQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalQualificationPolicy): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalQualificationPolicy>): MutableList<GlobalQualificationPolicy> {
        val matched = filter(items) { candidate: GlobalQualificationPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalQualificationPolicies(store: GlobalQualificationPolicyStore, query: GlobalQualificationPolicyQuery): MutableList<GlobalQualificationPolicy> {
    return query.apply(store.list())
}
