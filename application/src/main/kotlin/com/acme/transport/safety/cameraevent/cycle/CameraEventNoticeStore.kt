package com.acme.transport.safety.cameraevent.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event notices by code. */
interface CameraEventNoticeStore {
    fun find(code: String): CameraEventNotice?
    fun save(item: CameraEventNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventNoticeStore(
    var items: MutableMap<String, CameraEventNotice>,
    var order: MutableList<String>,
) : CameraEventNoticeStore {
    override fun find(code: String): CameraEventNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventNotice) {
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

    override fun list(): MutableList<CameraEventNotice> {
        val out = mutableListOf<CameraEventNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventNoticeStore(): CameraEventNoticeStore {
    return MemoryCameraEventNoticeStore(mutableMapOf<String, CameraEventNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventNoticeStore(
    var inner: CameraEventNoticeStore,
    var cache: TtlCache<CameraEventNotice>,
    var hits: Int,
    var misses: Int,
) : CameraEventNoticeStore {
    override fun find(code: String): CameraEventNotice? {
        val hit = (cache.get(code) as CameraEventNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventNotice> = inner.list()
}

fun newCachedCameraEventNoticeStore(inner: CameraEventNoticeStore, ttl: Int, clock: Clock): CameraEventNoticeStore {
    return CachedCameraEventNoticeStore(
        inner = inner,
        cache = newTtlCache<CameraEventNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event notices. Empty fields match all. */
data class CameraEventNoticeQuery(
    var label: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventNotice): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventNotice>): MutableList<CameraEventNotice> {
        val matched = filter(items) { candidate: CameraEventNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventNotices(store: CameraEventNoticeStore, query: CameraEventNoticeQuery): MutableList<CameraEventNotice> {
    return query.apply(store.list())
}
