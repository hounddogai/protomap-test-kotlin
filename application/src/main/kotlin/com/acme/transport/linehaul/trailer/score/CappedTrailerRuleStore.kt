package com.acme.transport.linehaul.trailer.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped trailer rules by code. */
interface CappedTrailerRuleStore {
    fun find(code: String): CappedTrailerRule?
    fun save(item: CappedTrailerRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTrailerRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTrailerRuleStore(
    var items: MutableMap<String, CappedTrailerRule>,
    var order: MutableList<String>,
) : CappedTrailerRuleStore {
    override fun find(code: String): CappedTrailerRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTrailerRule) {
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

    override fun list(): MutableList<CappedTrailerRule> {
        val out = mutableListOf<CappedTrailerRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTrailerRuleStore(): CappedTrailerRuleStore {
    return MemoryCappedTrailerRuleStore(mutableMapOf<String, CappedTrailerRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTrailerRuleStore(
    var inner: CappedTrailerRuleStore,
    var cache: TtlCache<CappedTrailerRule>,
    var hits: Int,
    var misses: Int,
) : CappedTrailerRuleStore {
    override fun find(code: String): CappedTrailerRule? {
        val hit = (cache.get(code) as CappedTrailerRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTrailerRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTrailerRule> = inner.list()
}

fun newCachedCappedTrailerRuleStore(inner: CappedTrailerRuleStore, ttl: Int, clock: Clock): CappedTrailerRuleStore {
    return CachedCappedTrailerRuleStore(
        inner = inner,
        cache = newTtlCache<CappedTrailerRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped trailer rules. Empty fields match all. */
data class CappedTrailerRuleQuery(
    var source: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTrailerRule): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedTrailerRule>): MutableList<CappedTrailerRule> {
        val matched = filter(items) { candidate: CappedTrailerRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTrailerRules(store: CappedTrailerRuleStore, query: CappedTrailerRuleQuery): MutableList<CappedTrailerRule> {
    return query.apply(store.list())
}
