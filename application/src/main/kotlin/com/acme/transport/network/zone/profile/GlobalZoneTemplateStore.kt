package com.acme.transport.network.zone.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global zone templates by code. */
interface GlobalZoneTemplateStore {
    fun find(code: String): GlobalZoneTemplate?
    fun save(item: GlobalZoneTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalZoneTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalZoneTemplateStore(
    var items: MutableMap<String, GlobalZoneTemplate>,
    var order: MutableList<String>,
) : GlobalZoneTemplateStore {
    override fun find(code: String): GlobalZoneTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalZoneTemplate) {
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

    override fun list(): MutableList<GlobalZoneTemplate> {
        val out = mutableListOf<GlobalZoneTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalZoneTemplateStore(): GlobalZoneTemplateStore {
    return MemoryGlobalZoneTemplateStore(mutableMapOf<String, GlobalZoneTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalZoneTemplateStore(
    var inner: GlobalZoneTemplateStore,
    var cache: TtlCache<GlobalZoneTemplate>,
    var hits: Int,
    var misses: Int,
) : GlobalZoneTemplateStore {
    override fun find(code: String): GlobalZoneTemplate? {
        val hit = (cache.get(code) as GlobalZoneTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalZoneTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalZoneTemplate> = inner.list()
}

fun newCachedGlobalZoneTemplateStore(inner: GlobalZoneTemplateStore, ttl: Int, clock: Clock): GlobalZoneTemplateStore {
    return CachedGlobalZoneTemplateStore(
        inner = inner,
        cache = newTtlCache<GlobalZoneTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global zone templates. Empty fields match all. */
data class GlobalZoneTemplateQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalZoneTemplate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalZoneTemplate>): MutableList<GlobalZoneTemplate> {
        val matched = filter(items) { candidate: GlobalZoneTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalZoneTemplates(store: GlobalZoneTemplateStore, query: GlobalZoneTemplateQuery): MutableList<GlobalZoneTemplate> {
    return query.apply(store.list())
}
