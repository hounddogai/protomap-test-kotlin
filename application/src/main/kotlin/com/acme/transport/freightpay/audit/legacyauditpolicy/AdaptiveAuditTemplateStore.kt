package com.acme.transport.freightpay.audit.legacyauditpolicy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive audit templates by code. */
interface AdaptiveAuditTemplateStore {
    fun find(code: String): AdaptiveAuditTemplate?
    fun save(item: AdaptiveAuditTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveAuditTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveAuditTemplateStore(
    var items: MutableMap<String, AdaptiveAuditTemplate>,
    var order: MutableList<String>,
) : AdaptiveAuditTemplateStore {
    override fun find(code: String): AdaptiveAuditTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveAuditTemplate) {
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

    override fun list(): MutableList<AdaptiveAuditTemplate> {
        val out = mutableListOf<AdaptiveAuditTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveAuditTemplateStore(): AdaptiveAuditTemplateStore {
    return MemoryAdaptiveAuditTemplateStore(
        mutableMapOf<String, AdaptiveAuditTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveAuditTemplateStore(
    var inner: AdaptiveAuditTemplateStore,
    var cache: TtlCache<AdaptiveAuditTemplate>,
    var hits: Int,
    var misses: Int,
) : AdaptiveAuditTemplateStore {
    override fun find(code: String): AdaptiveAuditTemplate? {
        val hit = (cache.get(code) as AdaptiveAuditTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveAuditTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveAuditTemplate> = inner.list()
}

fun newCachedAdaptiveAuditTemplateStore(inner: AdaptiveAuditTemplateStore, ttl: Int, clock: Clock): AdaptiveAuditTemplateStore {
    return CachedAdaptiveAuditTemplateStore(
        inner = inner,
        cache = newTtlCache<AdaptiveAuditTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive audit templates. Empty fields match all. */
data class AdaptiveAuditTemplateQuery(
    var unit: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveAuditTemplate): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveAuditTemplate>): MutableList<AdaptiveAuditTemplate> {
        val matched = filter(items) { candidate: AdaptiveAuditTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveAuditTemplates(store: AdaptiveAuditTemplateStore, query: AdaptiveAuditTemplateQuery): MutableList<AdaptiveAuditTemplate> {
    return query.apply(store.list())
}
