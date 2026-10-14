package com.acme.transport.safety.nearmiss.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss audits by code. */
interface NearMissAuditStore {
    fun find(code: String): NearMissAudit?
    fun save(item: NearMissAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissAuditStore(
    var items: MutableMap<String, NearMissAudit>,
    var order: MutableList<String>,
) : NearMissAuditStore {
    override fun find(code: String): NearMissAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissAudit) {
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

    override fun list(): MutableList<NearMissAudit> {
        val out = mutableListOf<NearMissAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissAuditStore(): NearMissAuditStore {
    return MemoryNearMissAuditStore(mutableMapOf<String, NearMissAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissAuditStore(
    var inner: NearMissAuditStore,
    var cache: TtlCache<NearMissAudit>,
    var hits: Int,
    var misses: Int,
) : NearMissAuditStore {
    override fun find(code: String): NearMissAudit? {
        val hit = (cache.get(code) as NearMissAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissAudit> = inner.list()
}

fun newCachedNearMissAuditStore(inner: NearMissAuditStore, ttl: Int, clock: Clock): NearMissAuditStore {
    return CachedNearMissAuditStore(
        inner = inner,
        cache = newTtlCache<NearMissAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss audits. Empty fields match all. */
data class NearMissAuditQuery(
    var country: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissAudit): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissAudit>): MutableList<NearMissAudit> {
        val matched = filter(items) { candidate: NearMissAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissAudits(store: NearMissAuditStore, query: NearMissAuditQuery): MutableList<NearMissAudit> {
    return query.apply(store.list())
}
