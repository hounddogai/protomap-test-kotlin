package com.acme.transport.linehaul.relay.relaythreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy relay rules by code. */
interface LegacyRelayRuleStore {
    fun find(code: String): LegacyRelayRule?
    fun save(item: LegacyRelayRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyRelayRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyRelayRuleStore(
    var items: MutableMap<String, LegacyRelayRule>,
    var order: MutableList<String>,
) : LegacyRelayRuleStore {
    override fun find(code: String): LegacyRelayRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyRelayRule) {
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

    override fun list(): MutableList<LegacyRelayRule> {
        val out = mutableListOf<LegacyRelayRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyRelayRuleStore(): LegacyRelayRuleStore {
    return MemoryLegacyRelayRuleStore(mutableMapOf<String, LegacyRelayRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyRelayRuleStore(
    var inner: LegacyRelayRuleStore,
    var cache: TtlCache<LegacyRelayRule>,
    var hits: Int,
    var misses: Int,
) : LegacyRelayRuleStore {
    override fun find(code: String): LegacyRelayRule? {
        val hit = (cache.get(code) as LegacyRelayRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyRelayRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyRelayRule> = inner.list()
}

fun newCachedLegacyRelayRuleStore(inner: LegacyRelayRuleStore, ttl: Int, clock: Clock): LegacyRelayRuleStore {
    return CachedLegacyRelayRuleStore(
        inner = inner,
        cache = newTtlCache<LegacyRelayRule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy relay rules. Empty fields match all. */
data class LegacyRelayRuleQuery(
    var lane: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyRelayRule): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyRelayRule>): MutableList<LegacyRelayRule> {
        val matched = filter(items) { candidate: LegacyRelayRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyRelayRules(store: LegacyRelayRuleStore, query: LegacyRelayRuleQuery): MutableList<LegacyRelayRule> {
    return query.apply(store.list())
}
