package com.acme.transport.freightpay.dispute.disputetemplate

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient dispute templates by code. */
interface LenientDisputeTemplateStore {
    fun find(code: String): LenientDisputeTemplate?
    fun save(item: LenientDisputeTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDisputeTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDisputeTemplateStore(
    var items: MutableMap<String, LenientDisputeTemplate>,
    var order: MutableList<String>,
) : LenientDisputeTemplateStore {
    override fun find(code: String): LenientDisputeTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDisputeTemplate) {
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

    override fun list(): MutableList<LenientDisputeTemplate> {
        val out = mutableListOf<LenientDisputeTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDisputeTemplateStore(): LenientDisputeTemplateStore {
    return MemoryLenientDisputeTemplateStore(
        mutableMapOf<String, LenientDisputeTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDisputeTemplateStore(
    var inner: LenientDisputeTemplateStore,
    var cache: TtlCache<LenientDisputeTemplate>,
    var hits: Int,
    var misses: Int,
) : LenientDisputeTemplateStore {
    override fun find(code: String): LenientDisputeTemplate? {
        val hit = (cache.get(code) as LenientDisputeTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDisputeTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDisputeTemplate> = inner.list()
}

fun newCachedLenientDisputeTemplateStore(inner: LenientDisputeTemplateStore, ttl: Int, clock: Clock): LenientDisputeTemplateStore {
    return CachedLenientDisputeTemplateStore(
        inner = inner,
        cache = newTtlCache<LenientDisputeTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient dispute templates. Empty fields match all. */
data class LenientDisputeTemplateQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDisputeTemplate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientDisputeTemplate>): MutableList<LenientDisputeTemplate> {
        val matched = filter(items) { candidate: LenientDisputeTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDisputeTemplates(store: LenientDisputeTemplateStore, query: LenientDisputeTemplateQuery): MutableList<LenientDisputeTemplate> {
    return query.apply(store.list())
}
