package com.acme.transport.terminal.sortation.sortationtier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation audits by code. */
interface SortationAuditStore {
    fun find(code: String): SortationAudit?
    fun save(item: SortationAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationAuditStore(
    var items: MutableMap<String, SortationAudit>,
    var order: MutableList<String>,
) : SortationAuditStore {
    override fun find(code: String): SortationAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationAudit) {
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

    override fun list(): MutableList<SortationAudit> {
        val out = mutableListOf<SortationAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationAuditStore(): SortationAuditStore {
    return MemorySortationAuditStore(mutableMapOf<String, SortationAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationAuditStore(
    var inner: SortationAuditStore,
    var cache: TtlCache<SortationAudit>,
    var hits: Int,
    var misses: Int,
) : SortationAuditStore {
    override fun find(code: String): SortationAudit? {
        val hit = (cache.get(code) as SortationAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationAudit> = inner.list()
}

fun newCachedSortationAuditStore(inner: SortationAuditStore, ttl: Int, clock: Clock): SortationAuditStore {
    return CachedSortationAuditStore(
        inner = inner,
        cache = newTtlCache<SortationAudit>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation audits. Empty fields match all. */
data class SortationAuditQuery(
    var depot: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationAudit): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SortationAudit>): MutableList<SortationAudit> {
        val matched = filter(items) { candidate: SortationAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationAudits(store: SortationAuditStore, query: SortationAuditQuery): MutableList<SortationAudit> {
    return query.apply(store.list())
}
