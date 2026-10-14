package com.acme.transport.terminal.traileryard.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard audits by code. */
interface TrailerYardAuditStore {
    fun find(code: String): TrailerYardAudit?
    fun save(item: TrailerYardAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardAuditStore(
    var items: MutableMap<String, TrailerYardAudit>,
    var order: MutableList<String>,
) : TrailerYardAuditStore {
    override fun find(code: String): TrailerYardAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardAudit) {
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

    override fun list(): MutableList<TrailerYardAudit> {
        val out = mutableListOf<TrailerYardAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardAuditStore(): TrailerYardAuditStore {
    return MemoryTrailerYardAuditStore(mutableMapOf<String, TrailerYardAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardAuditStore(
    var inner: TrailerYardAuditStore,
    var cache: TtlCache<TrailerYardAudit>,
    var hits: Int,
    var misses: Int,
) : TrailerYardAuditStore {
    override fun find(code: String): TrailerYardAudit? {
        val hit = (cache.get(code) as TrailerYardAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardAudit> = inner.list()
}

fun newCachedTrailerYardAuditStore(inner: TrailerYardAuditStore, ttl: Int, clock: Clock): TrailerYardAuditStore {
    return CachedTrailerYardAuditStore(
        inner = inner,
        cache = newTtlCache<TrailerYardAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard audits. Empty fields match all. */
data class TrailerYardAuditQuery(
    var region: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardAudit): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardAudit>): MutableList<TrailerYardAudit> {
        val matched = filter(items) { candidate: TrailerYardAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardAudits(store: TrailerYardAuditStore, query: TrailerYardAuditQuery): MutableList<TrailerYardAudit> {
    return query.apply(store.list())
}
