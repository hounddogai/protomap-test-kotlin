package com.acme.transport.safety.scorecard.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps manual scorecard audits by code. */
interface ManualScorecardAuditStore {
    fun find(code: String): ManualScorecardAudit?
    fun save(item: ManualScorecardAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualScorecardAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualScorecardAuditStore(
    var items: MutableMap<String, ManualScorecardAudit>,
    var order: MutableList<String>,
) : ManualScorecardAuditStore {
    override fun find(code: String): ManualScorecardAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualScorecardAudit) {
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

    override fun list(): MutableList<ManualScorecardAudit> {
        val out = mutableListOf<ManualScorecardAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualScorecardAuditStore(): ManualScorecardAuditStore {
    return MemoryManualScorecardAuditStore(
        mutableMapOf<String, ManualScorecardAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualScorecardAuditStore(
    var inner: ManualScorecardAuditStore,
    var cache: TtlCache<ManualScorecardAudit>,
    var hits: Int,
    var misses: Int,
) : ManualScorecardAuditStore {
    override fun find(code: String): ManualScorecardAudit? {
        val hit = (cache.get(code) as ManualScorecardAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualScorecardAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualScorecardAudit> = inner.list()
}

fun newCachedManualScorecardAuditStore(inner: ManualScorecardAuditStore, ttl: Int, clock: Clock): ManualScorecardAuditStore {
    return CachedManualScorecardAuditStore(
        inner = inner,
        cache = newTtlCache<ManualScorecardAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual scorecard audits. Empty fields match all. */
data class ManualScorecardAuditQuery(
    var name: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualScorecardAudit): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ManualScorecardAudit>): MutableList<ManualScorecardAudit> {
        val matched = filter(items) { candidate: ManualScorecardAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualScorecardAudits(store: ManualScorecardAuditStore, query: ManualScorecardAuditQuery): MutableList<ManualScorecardAudit> {
    return query.apply(store.list())
}
