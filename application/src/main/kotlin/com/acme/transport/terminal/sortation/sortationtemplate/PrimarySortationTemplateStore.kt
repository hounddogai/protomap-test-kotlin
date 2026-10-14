package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps primary sortation templates by code. */
interface PrimarySortationTemplateStore {
    fun find(code: String): PrimarySortationTemplate?
    fun save(item: PrimarySortationTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimarySortationTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimarySortationTemplateStore(
    var items: MutableMap<String, PrimarySortationTemplate>,
    var order: MutableList<String>,
) : PrimarySortationTemplateStore {
    override fun find(code: String): PrimarySortationTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimarySortationTemplate) {
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

    override fun list(): MutableList<PrimarySortationTemplate> {
        val out = mutableListOf<PrimarySortationTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimarySortationTemplateStore(): PrimarySortationTemplateStore {
    return MemoryPrimarySortationTemplateStore(
        mutableMapOf<String, PrimarySortationTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimarySortationTemplateStore(
    var inner: PrimarySortationTemplateStore,
    var cache: TtlCache<PrimarySortationTemplate>,
    var hits: Int,
    var misses: Int,
) : PrimarySortationTemplateStore {
    override fun find(code: String): PrimarySortationTemplate? {
        val hit = (cache.get(code) as PrimarySortationTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimarySortationTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimarySortationTemplate> = inner.list()
}

fun newCachedPrimarySortationTemplateStore(inner: PrimarySortationTemplateStore, ttl: Int, clock: Clock): PrimarySortationTemplateStore {
    return CachedPrimarySortationTemplateStore(
        inner = inner,
        cache = newTtlCache<PrimarySortationTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary sortation templates. Empty fields match all. */
data class PrimarySortationTemplateQuery(
    var lane: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimarySortationTemplate): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimarySortationTemplate>): MutableList<PrimarySortationTemplate> {
        val matched = filter(items) { candidate: PrimarySortationTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimarySortationTemplates(store: PrimarySortationTemplateStore, query: PrimarySortationTemplateQuery): MutableList<PrimarySortationTemplate> {
    return query.apply(store.list())
}
