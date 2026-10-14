package com.acme.transport.lastmile.drop.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop audits by code. */
interface DropAuditStore {
    fun find(code: String): DropAudit?
    fun save(item: DropAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropAuditStore(
    var items: MutableMap<String, DropAudit>,
    var order: MutableList<String>,
) : DropAuditStore {
    override fun find(code: String): DropAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropAudit) {
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

    override fun list(): MutableList<DropAudit> {
        val out = mutableListOf<DropAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropAuditStore(): DropAuditStore {
    return MemoryDropAuditStore(mutableMapOf<String, DropAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropAuditStore(
    var inner: DropAuditStore,
    var cache: TtlCache<DropAudit>,
    var hits: Int,
    var misses: Int,
) : DropAuditStore {
    override fun find(code: String): DropAudit? {
        val hit = (cache.get(code) as DropAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropAudit> = inner.list()
}

fun newCachedDropAuditStore(inner: DropAuditStore, ttl: Int, clock: Clock): DropAuditStore {
    return CachedDropAuditStore(
        inner = inner,
        cache = newTtlCache<DropAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop audits. Empty fields match all. */
data class DropAuditQuery(
    var locale: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropAudit): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropAudit>): MutableList<DropAudit> {
        val matched = filter(items) { candidate: DropAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropAudits(store: DropAuditStore, query: DropAuditQuery): MutableList<DropAudit> {
    return query.apply(store.list())
}
