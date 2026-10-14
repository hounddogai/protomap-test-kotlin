package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient rate card templates by code. */
interface LenientRateCardTemplateStore {
    fun find(code: String): LenientRateCardTemplate?
    fun save(item: LenientRateCardTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientRateCardTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientRateCardTemplateStore(
    var items: MutableMap<String, LenientRateCardTemplate>,
    var order: MutableList<String>,
) : LenientRateCardTemplateStore {
    override fun find(code: String): LenientRateCardTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientRateCardTemplate) {
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

    override fun list(): MutableList<LenientRateCardTemplate> {
        val out = mutableListOf<LenientRateCardTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientRateCardTemplateStore(): LenientRateCardTemplateStore {
    return MemoryLenientRateCardTemplateStore(
        mutableMapOf<String, LenientRateCardTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientRateCardTemplateStore(
    var inner: LenientRateCardTemplateStore,
    var cache: TtlCache<LenientRateCardTemplate>,
    var hits: Int,
    var misses: Int,
) : LenientRateCardTemplateStore {
    override fun find(code: String): LenientRateCardTemplate? {
        val hit = (cache.get(code) as LenientRateCardTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientRateCardTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientRateCardTemplate> = inner.list()
}

fun newCachedLenientRateCardTemplateStore(inner: LenientRateCardTemplateStore, ttl: Int, clock: Clock): LenientRateCardTemplateStore {
    return CachedLenientRateCardTemplateStore(
        inner = inner,
        cache = newTtlCache<LenientRateCardTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient rate card templates. Empty fields match all. */
data class LenientRateCardTemplateQuery(
    var source: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientRateCardTemplate): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientRateCardTemplate>): MutableList<LenientRateCardTemplate> {
        val matched = filter(items) { candidate: LenientRateCardTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientRateCardTemplates(store: LenientRateCardTemplateStore, query: LenientRateCardTemplateQuery): MutableList<LenientRateCardTemplate> {
    return query.apply(store.list())
}
