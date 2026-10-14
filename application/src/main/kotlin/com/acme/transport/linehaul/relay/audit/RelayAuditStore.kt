package com.acme.transport.linehaul.relay.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay audits by code. */
interface RelayAuditStore {
    fun find(code: String): RelayAudit?
    fun save(item: RelayAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayAuditStore(
    var items: MutableMap<String, RelayAudit>,
    var order: MutableList<String>,
) : RelayAuditStore {
    override fun find(code: String): RelayAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayAudit) {
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

    override fun list(): MutableList<RelayAudit> {
        val out = mutableListOf<RelayAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayAuditStore(): RelayAuditStore {
    return MemoryRelayAuditStore(mutableMapOf<String, RelayAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayAuditStore(
    var inner: RelayAuditStore,
    var cache: TtlCache<RelayAudit>,
    var hits: Int,
    var misses: Int,
) : RelayAuditStore {
    override fun find(code: String): RelayAudit? {
        val hit = (cache.get(code) as RelayAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayAudit> = inner.list()
}

fun newCachedRelayAuditStore(inner: RelayAuditStore, ttl: Int, clock: Clock): RelayAuditStore {
    return CachedRelayAuditStore(
        inner = inner,
        cache = newTtlCache<RelayAudit>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay audits. Empty fields match all. */
data class RelayAuditQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayAudit): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayAudit>): MutableList<RelayAudit> {
        val matched = filter(items) { candidate: RelayAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayAudits(store: RelayAuditStore, query: RelayAuditQuery): MutableList<RelayAudit> {
    return query.apply(store.list())
}
