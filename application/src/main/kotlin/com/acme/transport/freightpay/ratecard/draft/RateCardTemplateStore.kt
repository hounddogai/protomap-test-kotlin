package com.acme.transport.freightpay.ratecard.draft

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card templates by code. */
interface RateCardTemplateStore {
    fun find(code: String): RateCardTemplate?
    fun save(item: RateCardTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardTemplateStore(
    var items: MutableMap<String, RateCardTemplate>,
    var order: MutableList<String>,
) : RateCardTemplateStore {
    override fun find(code: String): RateCardTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardTemplate) {
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

    override fun list(): MutableList<RateCardTemplate> {
        val out = mutableListOf<RateCardTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardTemplateStore(): RateCardTemplateStore {
    return MemoryRateCardTemplateStore(mutableMapOf<String, RateCardTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardTemplateStore(
    var inner: RateCardTemplateStore,
    var cache: TtlCache<RateCardTemplate>,
    var hits: Int,
    var misses: Int,
) : RateCardTemplateStore {
    override fun find(code: String): RateCardTemplate? {
        val hit = (cache.get(code) as RateCardTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardTemplate> = inner.list()
}

fun newCachedRateCardTemplateStore(inner: RateCardTemplateStore, ttl: Int, clock: Clock): RateCardTemplateStore {
    return CachedRateCardTemplateStore(
        inner = inner,
        cache = newTtlCache<RateCardTemplate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card templates. Empty fields match all. */
data class RateCardTemplateQuery(
    var locale: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardTemplate): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<RateCardTemplate>): MutableList<RateCardTemplate> {
        val matched = filter(items) { candidate: RateCardTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardTemplates(store: RateCardTemplateStore, query: RateCardTemplateQuery): MutableList<RateCardTemplate> {
    return query.apply(store.list())
}
