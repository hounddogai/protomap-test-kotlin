package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted dock door audits by code. */
interface WeightedDockDoorAuditStore {
    fun find(code: String): WeightedDockDoorAudit?
    fun save(item: WeightedDockDoorAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDockDoorAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDockDoorAuditStore(
    var items: MutableMap<String, WeightedDockDoorAudit>,
    var order: MutableList<String>,
) : WeightedDockDoorAuditStore {
    override fun find(code: String): WeightedDockDoorAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDockDoorAudit) {
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

    override fun list(): MutableList<WeightedDockDoorAudit> {
        val out = mutableListOf<WeightedDockDoorAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDockDoorAuditStore(): WeightedDockDoorAuditStore {
    return MemoryWeightedDockDoorAuditStore(
        mutableMapOf<String, WeightedDockDoorAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDockDoorAuditStore(
    var inner: WeightedDockDoorAuditStore,
    var cache: TtlCache<WeightedDockDoorAudit>,
    var hits: Int,
    var misses: Int,
) : WeightedDockDoorAuditStore {
    override fun find(code: String): WeightedDockDoorAudit? {
        val hit = (cache.get(code) as WeightedDockDoorAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDockDoorAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDockDoorAudit> = inner.list()
}

fun newCachedWeightedDockDoorAuditStore(inner: WeightedDockDoorAuditStore, ttl: Int, clock: Clock): WeightedDockDoorAuditStore {
    return CachedWeightedDockDoorAuditStore(
        inner = inner,
        cache = newTtlCache<WeightedDockDoorAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted dock door audits. Empty fields match all. */
data class WeightedDockDoorAuditQuery(
    var channel: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDockDoorAudit): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<WeightedDockDoorAudit>): MutableList<WeightedDockDoorAudit> {
        val matched = filter(items) { candidate: WeightedDockDoorAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDockDoorAudits(store: WeightedDockDoorAuditStore, query: WeightedDockDoorAuditQuery): MutableList<WeightedDockDoorAudit> {
    return query.apply(store.list())
}
