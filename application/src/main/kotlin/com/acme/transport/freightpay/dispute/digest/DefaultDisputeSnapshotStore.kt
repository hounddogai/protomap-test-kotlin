package com.acme.transport.freightpay.dispute.digest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default dispute snapshots by code. */
interface DefaultDisputeSnapshotStore {
    fun find(code: String): DefaultDisputeSnapshot?
    fun save(item: DefaultDisputeSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDisputeSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDisputeSnapshotStore(
    var items: MutableMap<String, DefaultDisputeSnapshot>,
    var order: MutableList<String>,
) : DefaultDisputeSnapshotStore {
    override fun find(code: String): DefaultDisputeSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDisputeSnapshot) {
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

    override fun list(): MutableList<DefaultDisputeSnapshot> {
        val out = mutableListOf<DefaultDisputeSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDisputeSnapshotStore(): DefaultDisputeSnapshotStore {
    return MemoryDefaultDisputeSnapshotStore(
        mutableMapOf<String, DefaultDisputeSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDisputeSnapshotStore(
    var inner: DefaultDisputeSnapshotStore,
    var cache: TtlCache<DefaultDisputeSnapshot>,
    var hits: Int,
    var misses: Int,
) : DefaultDisputeSnapshotStore {
    override fun find(code: String): DefaultDisputeSnapshot? {
        val hit = (cache.get(code) as DefaultDisputeSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDisputeSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDisputeSnapshot> = inner.list()
}

fun newCachedDefaultDisputeSnapshotStore(inner: DefaultDisputeSnapshotStore, ttl: Int, clock: Clock): DefaultDisputeSnapshotStore {
    return CachedDefaultDisputeSnapshotStore(
        inner = inner,
        cache = newTtlCache<DefaultDisputeSnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default dispute snapshots. Empty fields match all. */
data class DefaultDisputeSnapshotQuery(
    var label: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDisputeSnapshot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<DefaultDisputeSnapshot>): MutableList<DefaultDisputeSnapshot> {
        val matched = filter(items) { candidate: DefaultDisputeSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDisputeSnapshots(store: DefaultDisputeSnapshotStore, query: DefaultDisputeSnapshotQuery): MutableList<DefaultDisputeSnapshot> {
    return query.apply(store.list())
}
