package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending relay rules by code. */
interface PendingRelayRuleStore {
    fun find(code: String): PendingRelayRule?
    fun save(item: PendingRelayRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingRelayRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingRelayRuleStore(
    var items: MutableMap<String, PendingRelayRule>,
    var order: MutableList<String>,
) : PendingRelayRuleStore {
    override fun find(code: String): PendingRelayRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingRelayRule) {
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

    override fun list(): MutableList<PendingRelayRule> {
        val out = mutableListOf<PendingRelayRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingRelayRuleStore(): PendingRelayRuleStore {
    return MemoryPendingRelayRuleStore(mutableMapOf<String, PendingRelayRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingRelayRuleStore(
    var inner: PendingRelayRuleStore,
    var cache: TtlCache<PendingRelayRule>,
    var hits: Int,
    var misses: Int,
) : PendingRelayRuleStore {
    override fun find(code: String): PendingRelayRule? {
        val hit = (cache.get(code) as PendingRelayRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingRelayRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingRelayRule> = inner.list()
}

fun newCachedPendingRelayRuleStore(inner: PendingRelayRuleStore, ttl: Int, clock: Clock): PendingRelayRuleStore {
    return CachedPendingRelayRuleStore(
        inner = inner,
        cache = newTtlCache<PendingRelayRule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending relay rules. Empty fields match all. */
data class PendingRelayRuleQuery(
    var locale: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingRelayRule): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingRelayRule>): MutableList<PendingRelayRule> {
        val matched = filter(items) { candidate: PendingRelayRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingRelayRules(store: PendingRelayRuleStore, query: PendingRelayRuleQuery): MutableList<PendingRelayRule> {
    return query.apply(store.list())
}
