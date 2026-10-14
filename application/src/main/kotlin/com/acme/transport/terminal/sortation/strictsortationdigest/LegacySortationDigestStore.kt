package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy sortation digests by code. */
interface LegacySortationDigestStore {
    fun find(code: String): LegacySortationDigest?
    fun save(item: LegacySortationDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacySortationDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacySortationDigestStore(
    var items: MutableMap<String, LegacySortationDigest>,
    var order: MutableList<String>,
) : LegacySortationDigestStore {
    override fun find(code: String): LegacySortationDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacySortationDigest) {
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

    override fun list(): MutableList<LegacySortationDigest> {
        val out = mutableListOf<LegacySortationDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacySortationDigestStore(): LegacySortationDigestStore {
    return MemoryLegacySortationDigestStore(
        mutableMapOf<String, LegacySortationDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacySortationDigestStore(
    var inner: LegacySortationDigestStore,
    var cache: TtlCache<LegacySortationDigest>,
    var hits: Int,
    var misses: Int,
) : LegacySortationDigestStore {
    override fun find(code: String): LegacySortationDigest? {
        val hit = (cache.get(code) as LegacySortationDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacySortationDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacySortationDigest> = inner.list()
}

fun newCachedLegacySortationDigestStore(inner: LegacySortationDigestStore, ttl: Int, clock: Clock): LegacySortationDigestStore {
    return CachedLegacySortationDigestStore(
        inner = inner,
        cache = newTtlCache<LegacySortationDigest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy sortation digests. Empty fields match all. */
data class LegacySortationDigestQuery(
    var locale: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacySortationDigest): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacySortationDigest>): MutableList<LegacySortationDigest> {
        val matched = filter(items) { candidate: LegacySortationDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacySortationDigests(store: LegacySortationDigestStore, query: LegacySortationDigestQuery): MutableList<LegacySortationDigest> {
    return query.apply(store.list())
}
