package com.acme.transport.network.servicearea.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area rules by code. */
interface ServiceAreaRuleStore {
    fun find(code: String): ServiceAreaRule?
    fun save(item: ServiceAreaRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaRuleStore(
    var items: MutableMap<String, ServiceAreaRule>,
    var order: MutableList<String>,
) : ServiceAreaRuleStore {
    override fun find(code: String): ServiceAreaRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaRule) {
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

    override fun list(): MutableList<ServiceAreaRule> {
        val out = mutableListOf<ServiceAreaRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaRuleStore(): ServiceAreaRuleStore {
    return MemoryServiceAreaRuleStore(mutableMapOf<String, ServiceAreaRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaRuleStore(
    var inner: ServiceAreaRuleStore,
    var cache: TtlCache<ServiceAreaRule>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaRuleStore {
    override fun find(code: String): ServiceAreaRule? {
        val hit = (cache.get(code) as ServiceAreaRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaRule> = inner.list()
}

fun newCachedServiceAreaRuleStore(inner: ServiceAreaRuleStore, ttl: Int, clock: Clock): ServiceAreaRuleStore {
    return CachedServiceAreaRuleStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area rules. Empty fields match all. */
data class ServiceAreaRuleQuery(
    var unit: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaRule): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaRule>): MutableList<ServiceAreaRule> {
        val matched = filter(items) { candidate: ServiceAreaRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaRules(store: ServiceAreaRuleStore, query: ServiceAreaRuleQuery): MutableList<ServiceAreaRule> {
    return query.apply(store.list())
}
