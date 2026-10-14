package com.acme.transport.customs.tariff.milestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered tariff templates by code. */
interface TieredTariffTemplateStore {
    fun find(code: String): TieredTariffTemplate?
    fun save(item: TieredTariffTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredTariffTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredTariffTemplateStore(
    var items: MutableMap<String, TieredTariffTemplate>,
    var order: MutableList<String>,
) : TieredTariffTemplateStore {
    override fun find(code: String): TieredTariffTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredTariffTemplate) {
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

    override fun list(): MutableList<TieredTariffTemplate> {
        val out = mutableListOf<TieredTariffTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredTariffTemplateStore(): TieredTariffTemplateStore {
    return MemoryTieredTariffTemplateStore(
        mutableMapOf<String, TieredTariffTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredTariffTemplateStore(
    var inner: TieredTariffTemplateStore,
    var cache: TtlCache<TieredTariffTemplate>,
    var hits: Int,
    var misses: Int,
) : TieredTariffTemplateStore {
    override fun find(code: String): TieredTariffTemplate? {
        val hit = (cache.get(code) as TieredTariffTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredTariffTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredTariffTemplate> = inner.list()
}

fun newCachedTieredTariffTemplateStore(inner: TieredTariffTemplateStore, ttl: Int, clock: Clock): TieredTariffTemplateStore {
    return CachedTieredTariffTemplateStore(
        inner = inner,
        cache = newTtlCache<TieredTariffTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered tariff templates. Empty fields match all. */
data class TieredTariffTemplateQuery(
    var title: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredTariffTemplate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<TieredTariffTemplate>): MutableList<TieredTariffTemplate> {
        val matched = filter(items) { candidate: TieredTariffTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredTariffTemplates(store: TieredTariffTemplateStore, query: TieredTariffTemplateQuery): MutableList<TieredTariffTemplate> {
    return query.apply(store.list())
}
