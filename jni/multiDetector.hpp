#include <opencv2/core/core.hpp>
#include <opencv2/objdetect/objdetect.hpp>
//#include <opencv2/contrib/detection_based_tracker.hpp>
#include <string>

class MultiDetector{
public:
	MultiDetector(const std::string& hogCascadeFilename,const std::string& lbpCascadeFilename, int minFaceSize){
		//DetectionBasedTracker(hogCascadeFilename, params);
		if(!lbpCascade.load(hogCascadeFilename)){
			CV_Error(CV_StsBadArg, "DetectionBasedTracker::DetectionBasedTracker: Cannot load a cascade from the file '"+hogCascadeFilename+"'");
		}
		if(!hogCascade.load(lbpCascadeFilename)){
			CV_Error(CV_StsBadArg, "DetectionBasedTracker::DetectionBasedTracker: Cannot load a cascade from the file '"+lbpCascadeFilename+"'");
	    }
		mSize=minFaceSize;

	}
	void multiDetect(const cv::Mat& imageGray,std::vector<cv::Rect> &RectFaces){
		RectFaces.clear();

		lbpCascade.detectMultiScale(imageGray, RectFaces, 1.1, 3, 0, cv::Size(mSize,mSize));
		std::vector<cv::Rect> tmps;
		hogCascade.detectMultiScale(imageGray, tmps, 1.1, 3, 0, cv::Size(mSize,mSize));
		for(int i=0; i<tmps.size();++i){
			RectFaces.push_back((cv::Rect)tmps[i]);
		}

	}
	void setmSize(int sz){
		mSize=sz;
	}
	int mSize;
	cv::CascadeClassifier lbpCascade;
	cv::CascadeClassifier hogCascade;
};
