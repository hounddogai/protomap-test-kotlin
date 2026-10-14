package com.acme.transport.customs.broker.batch

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker templates by code. */
interface BrokerTemplateStore {
    fun find(code: String): BrokerTemplate?
    fun save(item: BrokerTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerTemplateStore(
    var items: MutableMap<String, BrokerTemplate>,
    var order: MutableList<String>,
) : BrokerTemplateStore {
    override fun find(code: String): BrokerTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerTemplate) {
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

    override fun list(): MutableList<BrokerTemplate> {
        val out = mutableListOf<BrokerTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerTemplateStore(): BrokerTemplateStore {
    return MemoryBrokerTemplateStore(mutableMapOf<String, BrokerTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerTemplateStore(
    var inner: BrokerTemplateStore,
    var cache: TtlCache<BrokerTemplate>,
    var hits: Int,
    var misses: Int,
) : BrokerTemplateStore {
    override fun find(code: String): BrokerTemplate? {
        val hit = (cache.get(code) as BrokerTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerTemplate> = inner.list()
}

fun newCachedBrokerTemplateStore(inner: BrokerTemplateStore, ttl: Int, clock: Clock): BrokerTemplateStore {
    return CachedBrokerTemplateStore(
        inner = inner,
        cache = newTtlCache<BrokerTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker templates. Empty fields match all. */
data class BrokerTemplateQuery(
    var label: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerTemplate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerTemplate>): MutableList<BrokerTemplate> {
        val matched = filter(items) { candidate: BrokerTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerTemplates(store: BrokerTemplateStore, query: BrokerTemplateQuery): MutableList<BrokerTemplate> {
    return query.apply(store.list())
}
