package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps seasonal cutoff snapshots by code. */
interface SeasonalCutoffSnapshotStore {
    fun find(code: String): SeasonalCutoffSnapshot?
    fun save(item: SeasonalCutoffSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalCutoffSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalCutoffSnapshotStore(
    var items: MutableMap<String, SeasonalCutoffSnapshot>,
    var order: MutableList<String>,
) : SeasonalCutoffSnapshotStore {
    override fun find(code: String): SeasonalCutoffSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalCutoffSnapshot) {
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

    override fun list(): MutableList<SeasonalCutoffSnapshot> {
        val out = mutableListOf<SeasonalCutoffSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalCutoffSnapshotStore(): SeasonalCutoffSnapshotStore {
    return MemorySeasonalCutoffSnapshotStore(
        mutableMapOf<String, SeasonalCutoffSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalCutoffSnapshotStore(
    var inner: SeasonalCutoffSnapshotStore,
    var cache: TtlCache<SeasonalCutoffSnapshot>,
    var hits: Int,
    var misses: Int,
) : SeasonalCutoffSnapshotStore {
    override fun find(code: String): SeasonalCutoffSnapshot? {
        val hit = (cache.get(code) as SeasonalCutoffSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalCutoffSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalCutoffSnapshot> = inner.list()
}

fun newCachedSeasonalCutoffSnapshotStore(inner: SeasonalCutoffSnapshotStore, ttl: Int, clock: Clock): SeasonalCutoffSnapshotStore {
    return CachedSeasonalCutoffSnapshotStore(
        inner = inner,
        cache = newTtlCache<SeasonalCutoffSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal cutoff snapshots. Empty fields match all. */
data class SeasonalCutoffSnapshotQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalCutoffSnapshot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<SeasonalCutoffSnapshot>): MutableList<SeasonalCutoffSnapshot> {
        val matched = filter(items) { candidate: SeasonalCutoffSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalCutoffSnapshots(store: SeasonalCutoffSnapshotStore, query: SeasonalCutoffSnapshotQuery): MutableList<SeasonalCutoffSnapshot> {
    return query.apply(store.list())
}
