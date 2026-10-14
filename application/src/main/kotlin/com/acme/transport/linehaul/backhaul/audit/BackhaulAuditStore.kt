package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul audits by code. */
interface BackhaulAuditStore {
    fun find(code: String): BackhaulAudit?
    fun save(item: BackhaulAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulAuditStore(
    var items: MutableMap<String, BackhaulAudit>,
    var order: MutableList<String>,
) : BackhaulAuditStore {
    override fun find(code: String): BackhaulAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulAudit) {
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

    override fun list(): MutableList<BackhaulAudit> {
        val out = mutableListOf<BackhaulAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulAuditStore(): BackhaulAuditStore {
    return MemoryBackhaulAuditStore(mutableMapOf<String, BackhaulAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulAuditStore(
    var inner: BackhaulAuditStore,
    var cache: TtlCache<BackhaulAudit>,
    var hits: Int,
    var misses: Int,
) : BackhaulAuditStore {
    override fun find(code: String): BackhaulAudit? {
        val hit = (cache.get(code) as BackhaulAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulAudit> = inner.list()
}

fun newCachedBackhaulAuditStore(inner: BackhaulAuditStore, ttl: Int, clock: Clock): BackhaulAuditStore {
    return CachedBackhaulAuditStore(
        inner = inner,
        cache = newTtlCache<BackhaulAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul audits. Empty fields match all. */
data class BackhaulAuditQuery(
    var channel: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulAudit): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulAudit>): MutableList<BackhaulAudit> {
        val matched = filter(items) { candidate: BackhaulAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulAudits(store: BackhaulAuditStore, query: BackhaulAuditQuery): MutableList<BackhaulAudit> {
    return query.apply(store.list())
}
