package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial rules by code. */
interface AccessorialRuleStore {
    fun find(code: String): AccessorialRule?
    fun save(item: AccessorialRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialRuleStore(
    var items: MutableMap<String, AccessorialRule>,
    var order: MutableList<String>,
) : AccessorialRuleStore {
    override fun find(code: String): AccessorialRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialRule) {
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

    override fun list(): MutableList<AccessorialRule> {
        val out = mutableListOf<AccessorialRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialRuleStore(): AccessorialRuleStore {
    return MemoryAccessorialRuleStore(mutableMapOf<String, AccessorialRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialRuleStore(
    var inner: AccessorialRuleStore,
    var cache: TtlCache<AccessorialRule>,
    var hits: Int,
    var misses: Int,
) : AccessorialRuleStore {
    override fun find(code: String): AccessorialRule? {
        val hit = (cache.get(code) as AccessorialRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialRule> = inner.list()
}

fun newCachedAccessorialRuleStore(inner: AccessorialRuleStore, ttl: Int, clock: Clock): AccessorialRuleStore {
    return CachedAccessorialRuleStore(
        inner = inner,
        cache = newTtlCache<AccessorialRule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial rules. Empty fields match all. */
data class AccessorialRuleQuery(
    var category: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialRule): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AccessorialRule>): MutableList<AccessorialRule> {
        val matched = filter(items) { candidate: AccessorialRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialRules(store: AccessorialRuleStore, query: AccessorialRuleQuery): MutableList<AccessorialRule> {
    return query.apply(store.list())
}
