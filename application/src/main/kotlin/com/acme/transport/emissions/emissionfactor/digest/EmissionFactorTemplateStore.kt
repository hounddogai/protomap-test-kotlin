package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor templates by code. */
interface EmissionFactorTemplateStore {
    fun find(code: String): EmissionFactorTemplate?
    fun save(item: EmissionFactorTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorTemplateStore(
    var items: MutableMap<String, EmissionFactorTemplate>,
    var order: MutableList<String>,
) : EmissionFactorTemplateStore {
    override fun find(code: String): EmissionFactorTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorTemplate) {
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

    override fun list(): MutableList<EmissionFactorTemplate> {
        val out = mutableListOf<EmissionFactorTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorTemplateStore(): EmissionFactorTemplateStore {
    return MemoryEmissionFactorTemplateStore(
        mutableMapOf<String, EmissionFactorTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorTemplateStore(
    var inner: EmissionFactorTemplateStore,
    var cache: TtlCache<EmissionFactorTemplate>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorTemplateStore {
    override fun find(code: String): EmissionFactorTemplate? {
        val hit = (cache.get(code) as EmissionFactorTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorTemplate> = inner.list()
}

fun newCachedEmissionFactorTemplateStore(inner: EmissionFactorTemplateStore, ttl: Int, clock: Clock): EmissionFactorTemplateStore {
    return CachedEmissionFactorTemplateStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor templates. Empty fields match all. */
data class EmissionFactorTemplateQuery(
    var channel: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorTemplate): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorTemplate>): MutableList<EmissionFactorTemplate> {
        val matched = filter(items) { candidate: EmissionFactorTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorTemplates(store: EmissionFactorTemplateStore, query: EmissionFactorTemplateQuery): MutableList<EmissionFactorTemplate> {
    return query.apply(store.list())
}
