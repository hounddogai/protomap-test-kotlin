package com.acme.transport.network.cutoff.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff audits by code. */
interface CutoffAuditStore {
    fun find(code: String): CutoffAudit?
    fun save(item: CutoffAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffAuditStore(
    var items: MutableMap<String, CutoffAudit>,
    var order: MutableList<String>,
) : CutoffAuditStore {
    override fun find(code: String): CutoffAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffAudit) {
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

    override fun list(): MutableList<CutoffAudit> {
        val out = mutableListOf<CutoffAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffAuditStore(): CutoffAuditStore {
    return MemoryCutoffAuditStore(mutableMapOf<String, CutoffAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffAuditStore(
    var inner: CutoffAuditStore,
    var cache: TtlCache<CutoffAudit>,
    var hits: Int,
    var misses: Int,
) : CutoffAuditStore {
    override fun find(code: String): CutoffAudit? {
        val hit = (cache.get(code) as CutoffAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffAudit> = inner.list()
}

fun newCachedCutoffAuditStore(inner: CutoffAuditStore, ttl: Int, clock: Clock): CutoffAuditStore {
    return CachedCutoffAuditStore(
        inner = inner,
        cache = newTtlCache<CutoffAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff audits. Empty fields match all. */
data class CutoffAuditQuery(
    var depot: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffAudit): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffAudit>): MutableList<CutoffAudit> {
        val matched = filter(items) { candidate: CutoffAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffAudits(store: CutoffAuditStore, query: CutoffAuditQuery): MutableList<CutoffAudit> {
    return query.apply(store.list())
}
