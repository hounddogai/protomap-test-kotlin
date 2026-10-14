package com.acme.transport.terminal.crossdock.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps manual cross dock audits by code. */
interface ManualCrossDockAuditStore {
    fun find(code: String): ManualCrossDockAudit?
    fun save(item: ManualCrossDockAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualCrossDockAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualCrossDockAuditStore(
    var items: MutableMap<String, ManualCrossDockAudit>,
    var order: MutableList<String>,
) : ManualCrossDockAuditStore {
    override fun find(code: String): ManualCrossDockAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualCrossDockAudit) {
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

    override fun list(): MutableList<ManualCrossDockAudit> {
        val out = mutableListOf<ManualCrossDockAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualCrossDockAuditStore(): ManualCrossDockAuditStore {
    return MemoryManualCrossDockAuditStore(
        mutableMapOf<String, ManualCrossDockAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualCrossDockAuditStore(
    var inner: ManualCrossDockAuditStore,
    var cache: TtlCache<ManualCrossDockAudit>,
    var hits: Int,
    var misses: Int,
) : ManualCrossDockAuditStore {
    override fun find(code: String): ManualCrossDockAudit? {
        val hit = (cache.get(code) as ManualCrossDockAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualCrossDockAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualCrossDockAudit> = inner.list()
}

fun newCachedManualCrossDockAuditStore(inner: ManualCrossDockAuditStore, ttl: Int, clock: Clock): ManualCrossDockAuditStore {
    return CachedManualCrossDockAuditStore(
        inner = inner,
        cache = newTtlCache<ManualCrossDockAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual cross dock audits. Empty fields match all. */
data class ManualCrossDockAuditQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualCrossDockAudit): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualCrossDockAudit>): MutableList<ManualCrossDockAudit> {
        val matched = filter(items) { candidate: ManualCrossDockAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualCrossDockAudits(store: ManualCrossDockAuditStore, query: ManualCrossDockAuditQuery): MutableList<ManualCrossDockAudit> {
    return query.apply(store.list())
}
