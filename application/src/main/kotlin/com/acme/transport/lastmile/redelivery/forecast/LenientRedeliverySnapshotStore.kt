package com.acme.transport.lastmile.redelivery.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps lenient redelivery snapshots by code. */
interface LenientRedeliverySnapshotStore {
    fun find(code: String): LenientRedeliverySnapshot?
    fun save(item: LenientRedeliverySnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientRedeliverySnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientRedeliverySnapshotStore(
    var items: MutableMap<String, LenientRedeliverySnapshot>,
    var order: MutableList<String>,
) : LenientRedeliverySnapshotStore {
    override fun find(code: String): LenientRedeliverySnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientRedeliverySnapshot) {
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

    override fun list(): MutableList<LenientRedeliverySnapshot> {
        val out = mutableListOf<LenientRedeliverySnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientRedeliverySnapshotStore(): LenientRedeliverySnapshotStore {
    return MemoryLenientRedeliverySnapshotStore(
        mutableMapOf<String, LenientRedeliverySnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientRedeliverySnapshotStore(
    var inner: LenientRedeliverySnapshotStore,
    var cache: TtlCache<LenientRedeliverySnapshot>,
    var hits: Int,
    var misses: Int,
) : LenientRedeliverySnapshotStore {
    override fun find(code: String): LenientRedeliverySnapshot? {
        val hit = (cache.get(code) as LenientRedeliverySnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientRedeliverySnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientRedeliverySnapshot> = inner.list()
}

fun newCachedLenientRedeliverySnapshotStore(inner: LenientRedeliverySnapshotStore, ttl: Int, clock: Clock): LenientRedeliverySnapshotStore {
    return CachedLenientRedeliverySnapshotStore(
        inner = inner,
        cache = newTtlCache<LenientRedeliverySnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient redelivery snapshots. Empty fields match all. */
data class LenientRedeliverySnapshotQuery(
    var label: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientRedeliverySnapshot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientRedeliverySnapshot>): MutableList<LenientRedeliverySnapshot> {
        val matched = filter(items) { candidate: LenientRedeliverySnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientRedeliverySnapshots(store: LenientRedeliverySnapshotStore, query: LenientRedeliverySnapshotQuery): MutableList<LenientRedeliverySnapshot> {
    return query.apply(store.list())
}
