package com.acme.transport.customs.hold.holdaudit

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual hold audits by code. */
interface ManualHoldAuditStore {
    fun find(code: String): ManualHoldAudit?
    fun save(item: ManualHoldAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualHoldAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualHoldAuditStore(
    var items: MutableMap<String, ManualHoldAudit>,
    var order: MutableList<String>,
) : ManualHoldAuditStore {
    override fun find(code: String): ManualHoldAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualHoldAudit) {
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

    override fun list(): MutableList<ManualHoldAudit> {
        val out = mutableListOf<ManualHoldAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualHoldAuditStore(): ManualHoldAuditStore {
    return MemoryManualHoldAuditStore(mutableMapOf<String, ManualHoldAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualHoldAuditStore(
    var inner: ManualHoldAuditStore,
    var cache: TtlCache<ManualHoldAudit>,
    var hits: Int,
    var misses: Int,
) : ManualHoldAuditStore {
    override fun find(code: String): ManualHoldAudit? {
        val hit = (cache.get(code) as ManualHoldAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualHoldAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualHoldAudit> = inner.list()
}

fun newCachedManualHoldAuditStore(inner: ManualHoldAuditStore, ttl: Int, clock: Clock): ManualHoldAuditStore {
    return CachedManualHoldAuditStore(
        inner = inner,
        cache = newTtlCache<ManualHoldAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual hold audits. Empty fields match all. */
data class ManualHoldAuditQuery(
    var category: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualHoldAudit): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualHoldAudit>): MutableList<ManualHoldAudit> {
        val matched = filter(items) { candidate: ManualHoldAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualHoldAudits(store: ManualHoldAuditStore, query: ManualHoldAuditQuery): MutableList<ManualHoldAudit> {
    return query.apply(store.list())
}
