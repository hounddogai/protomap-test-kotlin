package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps global dock door templates by code. */
interface GlobalDockDoorTemplateStore {
    fun find(code: String): GlobalDockDoorTemplate?
    fun save(item: GlobalDockDoorTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalDockDoorTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalDockDoorTemplateStore(
    var items: MutableMap<String, GlobalDockDoorTemplate>,
    var order: MutableList<String>,
) : GlobalDockDoorTemplateStore {
    override fun find(code: String): GlobalDockDoorTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalDockDoorTemplate) {
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

    override fun list(): MutableList<GlobalDockDoorTemplate> {
        val out = mutableListOf<GlobalDockDoorTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalDockDoorTemplateStore(): GlobalDockDoorTemplateStore {
    return MemoryGlobalDockDoorTemplateStore(
        mutableMapOf<String, GlobalDockDoorTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalDockDoorTemplateStore(
    var inner: GlobalDockDoorTemplateStore,
    var cache: TtlCache<GlobalDockDoorTemplate>,
    var hits: Int,
    var misses: Int,
) : GlobalDockDoorTemplateStore {
    override fun find(code: String): GlobalDockDoorTemplate? {
        val hit = (cache.get(code) as GlobalDockDoorTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalDockDoorTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalDockDoorTemplate> = inner.list()
}

fun newCachedGlobalDockDoorTemplateStore(inner: GlobalDockDoorTemplateStore, ttl: Int, clock: Clock): GlobalDockDoorTemplateStore {
    return CachedGlobalDockDoorTemplateStore(
        inner = inner,
        cache = newTtlCache<GlobalDockDoorTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global dock door templates. Empty fields match all. */
data class GlobalDockDoorTemplateQuery(
    var name: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalDockDoorTemplate): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<GlobalDockDoorTemplate>): MutableList<GlobalDockDoorTemplate> {
        val matched = filter(items) { candidate: GlobalDockDoorTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalDockDoorTemplates(store: GlobalDockDoorTemplateStore, query: GlobalDockDoorTemplateQuery): MutableList<GlobalDockDoorTemplate> {
    return query.apply(store.list())
}
