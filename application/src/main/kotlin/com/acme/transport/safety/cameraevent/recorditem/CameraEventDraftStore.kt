package com.acme.transport.safety.cameraevent.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event drafts by code. */
interface CameraEventDraftStore {
    fun find(code: String): CameraEventDraft?
    fun save(item: CameraEventDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventDraftStore(
    var items: MutableMap<String, CameraEventDraft>,
    var order: MutableList<String>,
) : CameraEventDraftStore {
    override fun find(code: String): CameraEventDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventDraft) {
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

    override fun list(): MutableList<CameraEventDraft> {
        val out = mutableListOf<CameraEventDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventDraftStore(): CameraEventDraftStore {
    return MemoryCameraEventDraftStore(mutableMapOf<String, CameraEventDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventDraftStore(
    var inner: CameraEventDraftStore,
    var cache: TtlCache<CameraEventDraft>,
    var hits: Int,
    var misses: Int,
) : CameraEventDraftStore {
    override fun find(code: String): CameraEventDraft? {
        val hit = (cache.get(code) as CameraEventDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventDraft> = inner.list()
}

fun newCachedCameraEventDraftStore(inner: CameraEventDraftStore, ttl: Int, clock: Clock): CameraEventDraftStore {
    return CachedCameraEventDraftStore(
        inner = inner,
        cache = newTtlCache<CameraEventDraft>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event drafts. Empty fields match all. */
data class CameraEventDraftQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventDraft): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventDraft>): MutableList<CameraEventDraft> {
        val matched = filter(items) { candidate: CameraEventDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventDrafts(store: CameraEventDraftStore, query: CameraEventDraftQuery): MutableList<CameraEventDraft> {
    return query.apply(store.list())
}
