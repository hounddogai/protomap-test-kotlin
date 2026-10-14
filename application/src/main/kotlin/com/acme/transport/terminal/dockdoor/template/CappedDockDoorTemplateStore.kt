package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped dock door templates by code. */
interface CappedDockDoorTemplateStore {
    fun find(code: String): CappedDockDoorTemplate?
    fun save(item: CappedDockDoorTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDockDoorTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDockDoorTemplateStore(
    var items: MutableMap<String, CappedDockDoorTemplate>,
    var order: MutableList<String>,
) : CappedDockDoorTemplateStore {
    override fun find(code: String): CappedDockDoorTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDockDoorTemplate) {
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

    override fun list(): MutableList<CappedDockDoorTemplate> {
        val out = mutableListOf<CappedDockDoorTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDockDoorTemplateStore(): CappedDockDoorTemplateStore {
    return MemoryCappedDockDoorTemplateStore(
        mutableMapOf<String, CappedDockDoorTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDockDoorTemplateStore(
    var inner: CappedDockDoorTemplateStore,
    var cache: TtlCache<CappedDockDoorTemplate>,
    var hits: Int,
    var misses: Int,
) : CappedDockDoorTemplateStore {
    override fun find(code: String): CappedDockDoorTemplate? {
        val hit = (cache.get(code) as CappedDockDoorTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDockDoorTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDockDoorTemplate> = inner.list()
}

fun newCachedCappedDockDoorTemplateStore(inner: CappedDockDoorTemplateStore, ttl: Int, clock: Clock): CappedDockDoorTemplateStore {
    return CachedCappedDockDoorTemplateStore(
        inner = inner,
        cache = newTtlCache<CappedDockDoorTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped dock door templates. Empty fields match all. */
data class CappedDockDoorTemplateQuery(
    var channel: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDockDoorTemplate): Boolean {
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

    fun apply(items: MutableList<CappedDockDoorTemplate>): MutableList<CappedDockDoorTemplate> {
        val matched = filter(items) { candidate: CappedDockDoorTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDockDoorTemplates(store: CappedDockDoorTemplateStore, query: CappedDockDoorTemplateQuery): MutableList<CappedDockDoorTemplate> {
    return query.apply(store.list())
}
