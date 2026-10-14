package com.acme.transport.freightpay.audit.snapshot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit snapshots by code. */
interface AuditSnapshotStore {
    fun find(code: String): AuditSnapshot?
    fun save(item: AuditSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditSnapshotStore(
    var items: MutableMap<String, AuditSnapshot>,
    var order: MutableList<String>,
) : AuditSnapshotStore {
    override fun find(code: String): AuditSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditSnapshot) {
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

    override fun list(): MutableList<AuditSnapshot> {
        val out = mutableListOf<AuditSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditSnapshotStore(): AuditSnapshotStore {
    return MemoryAuditSnapshotStore(mutableMapOf<String, AuditSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditSnapshotStore(
    var inner: AuditSnapshotStore,
    var cache: TtlCache<AuditSnapshot>,
    var hits: Int,
    var misses: Int,
) : AuditSnapshotStore {
    override fun find(code: String): AuditSnapshot? {
        val hit = (cache.get(code) as AuditSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditSnapshot> = inner.list()
}

fun newCachedAuditSnapshotStore(inner: AuditSnapshotStore, ttl: Int, clock: Clock): AuditSnapshotStore {
    return CachedAuditSnapshotStore(
        inner = inner,
        cache = newTtlCache<AuditSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit snapshots. Empty fields match all. */
data class AuditSnapshotQuery(
    var locale: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditSnapshot): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditSnapshot>): MutableList<AuditSnapshot> {
        val matched = filter(items) { candidate: AuditSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditSnapshots(store: AuditSnapshotStore, query: AuditSnapshotQuery): MutableList<AuditSnapshot> {
    return query.apply(store.list())
}
