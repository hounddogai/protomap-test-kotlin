package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default proof of delivery templates by code. */
interface DefaultProofOfDeliveryTemplateStore {
    fun find(code: String): DefaultProofOfDeliveryTemplate?
    fun save(item: DefaultProofOfDeliveryTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultProofOfDeliveryTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultProofOfDeliveryTemplateStore(
    var items: MutableMap<String, DefaultProofOfDeliveryTemplate>,
    var order: MutableList<String>,
) : DefaultProofOfDeliveryTemplateStore {
    override fun find(code: String): DefaultProofOfDeliveryTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultProofOfDeliveryTemplate) {
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

    override fun list(): MutableList<DefaultProofOfDeliveryTemplate> {
        val out = mutableListOf<DefaultProofOfDeliveryTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultProofOfDeliveryTemplateStore(): DefaultProofOfDeliveryTemplateStore {
    return MemoryDefaultProofOfDeliveryTemplateStore(
        mutableMapOf<String, DefaultProofOfDeliveryTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultProofOfDeliveryTemplateStore(
    var inner: DefaultProofOfDeliveryTemplateStore,
    var cache: TtlCache<DefaultProofOfDeliveryTemplate>,
    var hits: Int,
    var misses: Int,
) : DefaultProofOfDeliveryTemplateStore {
    override fun find(code: String): DefaultProofOfDeliveryTemplate? {
        val hit = (cache.get(code) as DefaultProofOfDeliveryTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultProofOfDeliveryTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultProofOfDeliveryTemplate> = inner.list()
}

fun newCachedDefaultProofOfDeliveryTemplateStore(inner: DefaultProofOfDeliveryTemplateStore, ttl: Int, clock: Clock): DefaultProofOfDeliveryTemplateStore {
    return CachedDefaultProofOfDeliveryTemplateStore(
        inner = inner,
        cache = newTtlCache<DefaultProofOfDeliveryTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default proof of delivery templates. Empty fields match all. */
data class DefaultProofOfDeliveryTemplateQuery(
    var unit: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultProofOfDeliveryTemplate): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultProofOfDeliveryTemplate>): MutableList<DefaultProofOfDeliveryTemplate> {
        val matched = filter(items) { candidate: DefaultProofOfDeliveryTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultProofOfDeliveryTemplates(store: DefaultProofOfDeliveryTemplateStore, query: DefaultProofOfDeliveryTemplateQuery): MutableList<DefaultProofOfDeliveryTemplate> {
    return query.apply(store.list())
}
