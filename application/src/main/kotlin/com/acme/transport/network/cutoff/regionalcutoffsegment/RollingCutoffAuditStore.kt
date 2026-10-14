package com.acme.transport.network.cutoff.regionalcutoffsegment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling cutoff audits by code. */
interface RollingCutoffAuditStore {
    fun find(code: String): RollingCutoffAudit?
    fun save(item: RollingCutoffAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCutoffAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCutoffAuditStore(
    var items: MutableMap<String, RollingCutoffAudit>,
    var order: MutableList<String>,
) : RollingCutoffAuditStore {
    override fun find(code: String): RollingCutoffAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCutoffAudit) {
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

    override fun list(): MutableList<RollingCutoffAudit> {
        val out = mutableListOf<RollingCutoffAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCutoffAuditStore(): RollingCutoffAuditStore {
    return MemoryRollingCutoffAuditStore(mutableMapOf<String, RollingCutoffAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCutoffAuditStore(
    var inner: RollingCutoffAuditStore,
    var cache: TtlCache<RollingCutoffAudit>,
    var hits: Int,
    var misses: Int,
) : RollingCutoffAuditStore {
    override fun find(code: String): RollingCutoffAudit? {
        val hit = (cache.get(code) as RollingCutoffAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCutoffAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCutoffAudit> = inner.list()
}

fun newCachedRollingCutoffAuditStore(inner: RollingCutoffAuditStore, ttl: Int, clock: Clock): RollingCutoffAuditStore {
    return CachedRollingCutoffAuditStore(
        inner = inner,
        cache = newTtlCache<RollingCutoffAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling cutoff audits. Empty fields match all. */
data class RollingCutoffAuditQuery(
    var source: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCutoffAudit): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCutoffAudit>): MutableList<RollingCutoffAudit> {
        val matched = filter(items) { candidate: RollingCutoffAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCutoffAudits(store: RollingCutoffAuditStore, query: RollingCutoffAuditQuery): MutableList<RollingCutoffAudit> {
    return query.apply(store.list())
}
