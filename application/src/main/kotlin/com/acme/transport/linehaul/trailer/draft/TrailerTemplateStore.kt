package com.acme.transport.linehaul.trailer.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer templates by code. */
interface TrailerTemplateStore {
    fun find(code: String): TrailerTemplate?
    fun save(item: TrailerTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerTemplateStore(
    var items: MutableMap<String, TrailerTemplate>,
    var order: MutableList<String>,
) : TrailerTemplateStore {
    override fun find(code: String): TrailerTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerTemplate) {
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

    override fun list(): MutableList<TrailerTemplate> {
        val out = mutableListOf<TrailerTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerTemplateStore(): TrailerTemplateStore {
    return MemoryTrailerTemplateStore(mutableMapOf<String, TrailerTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerTemplateStore(
    var inner: TrailerTemplateStore,
    var cache: TtlCache<TrailerTemplate>,
    var hits: Int,
    var misses: Int,
) : TrailerTemplateStore {
    override fun find(code: String): TrailerTemplate? {
        val hit = (cache.get(code) as TrailerTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerTemplate> = inner.list()
}

fun newCachedTrailerTemplateStore(inner: TrailerTemplateStore, ttl: Int, clock: Clock): TrailerTemplateStore {
    return CachedTrailerTemplateStore(
        inner = inner,
        cache = newTtlCache<TrailerTemplate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer templates. Empty fields match all. */
data class TrailerTemplateQuery(
    var channel: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerTemplate): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerTemplate>): MutableList<TrailerTemplate> {
        val matched = filter(items) { candidate: TrailerTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerTemplates(store: TrailerTemplateStore, query: TrailerTemplateQuery): MutableList<TrailerTemplate> {
    return query.apply(store.list())
}
