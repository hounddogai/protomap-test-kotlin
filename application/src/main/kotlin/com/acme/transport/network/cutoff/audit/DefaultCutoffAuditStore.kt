package com.acme.transport.network.cutoff.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default cutoff audits by code. */
interface DefaultCutoffAuditStore {
    fun find(code: String): DefaultCutoffAudit?
    fun save(item: DefaultCutoffAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCutoffAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCutoffAuditStore(
    var items: MutableMap<String, DefaultCutoffAudit>,
    var order: MutableList<String>,
) : DefaultCutoffAuditStore {
    override fun find(code: String): DefaultCutoffAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCutoffAudit) {
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

    override fun list(): MutableList<DefaultCutoffAudit> {
        val out = mutableListOf<DefaultCutoffAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCutoffAuditStore(): DefaultCutoffAuditStore {
    return MemoryDefaultCutoffAuditStore(mutableMapOf<String, DefaultCutoffAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCutoffAuditStore(
    var inner: DefaultCutoffAuditStore,
    var cache: TtlCache<DefaultCutoffAudit>,
    var hits: Int,
    var misses: Int,
) : DefaultCutoffAuditStore {
    override fun find(code: String): DefaultCutoffAudit? {
        val hit = (cache.get(code) as DefaultCutoffAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCutoffAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCutoffAudit> = inner.list()
}

fun newCachedDefaultCutoffAuditStore(inner: DefaultCutoffAuditStore, ttl: Int, clock: Clock): DefaultCutoffAuditStore {
    return CachedDefaultCutoffAuditStore(
        inner = inner,
        cache = newTtlCache<DefaultCutoffAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default cutoff audits. Empty fields match all. */
data class DefaultCutoffAuditQuery(
    var lane: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCutoffAudit): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultCutoffAudit>): MutableList<DefaultCutoffAudit> {
        val matched = filter(items) { candidate: DefaultCutoffAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCutoffAudits(store: DefaultCutoffAuditStore, query: DefaultCutoffAuditQuery): MutableList<DefaultCutoffAudit> {
    return query.apply(store.list())
}
