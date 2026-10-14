package com.acme.transport.freightpay.ratecard.audit

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback rate card templates by code. */
interface FallbackRateCardTemplateStore {
    fun find(code: String): FallbackRateCardTemplate?
    fun save(item: FallbackRateCardTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRateCardTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRateCardTemplateStore(
    var items: MutableMap<String, FallbackRateCardTemplate>,
    var order: MutableList<String>,
) : FallbackRateCardTemplateStore {
    override fun find(code: String): FallbackRateCardTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRateCardTemplate) {
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

    override fun list(): MutableList<FallbackRateCardTemplate> {
        val out = mutableListOf<FallbackRateCardTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRateCardTemplateStore(): FallbackRateCardTemplateStore {
    return MemoryFallbackRateCardTemplateStore(
        mutableMapOf<String, FallbackRateCardTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRateCardTemplateStore(
    var inner: FallbackRateCardTemplateStore,
    var cache: TtlCache<FallbackRateCardTemplate>,
    var hits: Int,
    var misses: Int,
) : FallbackRateCardTemplateStore {
    override fun find(code: String): FallbackRateCardTemplate? {
        val hit = (cache.get(code) as FallbackRateCardTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRateCardTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRateCardTemplate> = inner.list()
}

fun newCachedFallbackRateCardTemplateStore(inner: FallbackRateCardTemplateStore, ttl: Int, clock: Clock): FallbackRateCardTemplateStore {
    return CachedFallbackRateCardTemplateStore(
        inner = inner,
        cache = newTtlCache<FallbackRateCardTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback rate card templates. Empty fields match all. */
data class FallbackRateCardTemplateQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRateCardTemplate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackRateCardTemplate>): MutableList<FallbackRateCardTemplate> {
        val matched = filter(items) { candidate: FallbackRateCardTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRateCardTemplates(store: FallbackRateCardTemplateStore, query: FallbackRateCardTemplateQuery): MutableList<FallbackRateCardTemplate> {
    return query.apply(store.list())
}
