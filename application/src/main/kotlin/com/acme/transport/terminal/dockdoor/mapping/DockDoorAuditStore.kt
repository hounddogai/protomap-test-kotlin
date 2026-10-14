package com.acme.transport.terminal.dockdoor.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door audits by code. */
interface DockDoorAuditStore {
    fun find(code: String): DockDoorAudit?
    fun save(item: DockDoorAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorAuditStore(
    var items: MutableMap<String, DockDoorAudit>,
    var order: MutableList<String>,
) : DockDoorAuditStore {
    override fun find(code: String): DockDoorAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorAudit) {
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

    override fun list(): MutableList<DockDoorAudit> {
        val out = mutableListOf<DockDoorAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorAuditStore(): DockDoorAuditStore {
    return MemoryDockDoorAuditStore(mutableMapOf<String, DockDoorAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorAuditStore(
    var inner: DockDoorAuditStore,
    var cache: TtlCache<DockDoorAudit>,
    var hits: Int,
    var misses: Int,
) : DockDoorAuditStore {
    override fun find(code: String): DockDoorAudit? {
        val hit = (cache.get(code) as DockDoorAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorAudit> = inner.list()
}

fun newCachedDockDoorAuditStore(inner: DockDoorAuditStore, ttl: Int, clock: Clock): DockDoorAuditStore {
    return CachedDockDoorAuditStore(
        inner = inner,
        cache = newTtlCache<DockDoorAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door audits. Empty fields match all. */
data class DockDoorAuditQuery(
    var name: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorAudit): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorAudit>): MutableList<DockDoorAudit> {
        val matched = filter(items) { candidate: DockDoorAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorAudits(store: DockDoorAuditStore, query: DockDoorAuditQuery): MutableList<DockDoorAudit> {
    return query.apply(store.list())
}
