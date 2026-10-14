package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute snapshots by code. */
interface DisputeSnapshotStore {
    fun find(code: String): DisputeSnapshot?
    fun save(item: DisputeSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeSnapshotStore(
    var items: MutableMap<String, DisputeSnapshot>,
    var order: MutableList<String>,
) : DisputeSnapshotStore {
    override fun find(code: String): DisputeSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeSnapshot) {
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

    override fun list(): MutableList<DisputeSnapshot> {
        val out = mutableListOf<DisputeSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeSnapshotStore(): DisputeSnapshotStore {
    return MemoryDisputeSnapshotStore(mutableMapOf<String, DisputeSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeSnapshotStore(
    var inner: DisputeSnapshotStore,
    var cache: TtlCache<DisputeSnapshot>,
    var hits: Int,
    var misses: Int,
) : DisputeSnapshotStore {
    override fun find(code: String): DisputeSnapshot? {
        val hit = (cache.get(code) as DisputeSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeSnapshot> = inner.list()
}

fun newCachedDisputeSnapshotStore(inner: DisputeSnapshotStore, ttl: Int, clock: Clock): DisputeSnapshotStore {
    return CachedDisputeSnapshotStore(
        inner = inner,
        cache = newTtlCache<DisputeSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute snapshots. Empty fields match all. */
data class DisputeSnapshotQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeSnapshot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeSnapshot>): MutableList<DisputeSnapshot> {
        val matched = filter(items) { candidate: DisputeSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeSnapshots(store: DisputeSnapshotStore, query: DisputeSnapshotQuery): MutableList<DisputeSnapshot> {
    return query.apply(store.list())
}
