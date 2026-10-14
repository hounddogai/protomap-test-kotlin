package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door templates by code. */
interface DockDoorTemplateStore {
    fun find(code: String): DockDoorTemplate?
    fun save(item: DockDoorTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorTemplateStore(
    var items: MutableMap<String, DockDoorTemplate>,
    var order: MutableList<String>,
) : DockDoorTemplateStore {
    override fun find(code: String): DockDoorTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorTemplate) {
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

    override fun list(): MutableList<DockDoorTemplate> {
        val out = mutableListOf<DockDoorTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorTemplateStore(): DockDoorTemplateStore {
    return MemoryDockDoorTemplateStore(mutableMapOf<String, DockDoorTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorTemplateStore(
    var inner: DockDoorTemplateStore,
    var cache: TtlCache<DockDoorTemplate>,
    var hits: Int,
    var misses: Int,
) : DockDoorTemplateStore {
    override fun find(code: String): DockDoorTemplate? {
        val hit = (cache.get(code) as DockDoorTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorTemplate> = inner.list()
}

fun newCachedDockDoorTemplateStore(inner: DockDoorTemplateStore, ttl: Int, clock: Clock): DockDoorTemplateStore {
    return CachedDockDoorTemplateStore(
        inner = inner,
        cache = newTtlCache<DockDoorTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door templates. Empty fields match all. */
data class DockDoorTemplateQuery(
    var country: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorTemplate): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<DockDoorTemplate>): MutableList<DockDoorTemplate> {
        val matched = filter(items) { candidate: DockDoorTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorTemplates(store: DockDoorTemplateStore, query: DockDoorTemplateQuery): MutableList<DockDoorTemplate> {
    return query.apply(store.list())
}
